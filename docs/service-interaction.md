# SmartStock Service Interaction & RabbitMQ Event Workflows

## 1. Overview of Asynchronous Messaging

SmartStock decouples state changes between the **Order Management Service** and **Inventory Management Service** via **RabbitMQ**. All messages are routed through a topic exchange named `smartstock.exchange`.

### Exchange and Queue Topology

| Queue Name | Routing Key | Publisher | Consumer | Purpose |
|------------|-------------|-----------|----------|---------|
| `order.created.queue` | `order.created` | Order Service | Inventory Service | Trigger stock reservation check |
| `inventory.reserved.queue` | `inventory.reserved` | Inventory Service | Order Service | Confirm order placement (`PLACED`) |
| `inventory.rejected.queue` | `inventory.rejected` | Inventory Service | Order Service | Handle out of stock / reject order |
| `order.cancelled.queue` | `order.cancelled` | Order Service | Inventory Service | Trigger reserved stock release |
| `inventory.released.queue` | `inventory.released` | Inventory Service | Order Service | Confirm stock restored (`CANCELLED`) |
| `smartstock.deadletter.queue` | `smartstock.deadletter` | DLX | Monitoring / Admin | Catch repeated processing failures |

---

## 2. Sequence Diagrams

### 2.1 Scenario 1: Successful Order Placement & Stock Reservation

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Customer (React UI)
    participant OS as Order Service (8081)
    participant ODB as order_db
    participant RMQ as RabbitMQ (smartstock.exchange)
    participant IS as Inventory Service (8082)
    participant IDB as inventory_db

    Customer->>OS: POST /api/orders (productId, qty, customerName)
    OS->>ODB: INSERT Order (status: PENDING)
    OS->>RMQ: Publish OrderCreated Event
    OS-->>Customer: 201 Created (Order id, status: PENDING)
    
    RMQ->>IS: Deliver OrderCreated (order.created.queue)
    Note over IS,IDB: Atomic Stock Check & Reservation
    IS->>IDB: UPDATE products SET available = available - qty, reserved = reserved + qty WHERE available >= qty
    IS->>IDB: INSERT inventory_reservations (orderId, productId, qty, status: RESERVED)
    IS->>RMQ: Publish InventoryReserved Event
    
    RMQ->>OS: Deliver InventoryReserved (inventory.reserved.queue)
    OS->>ODB: UPDATE orders SET status = 'PLACED' WHERE id = orderId
    
    Customer->>OS: Polling / Refresh: GET /api/orders/customer/{name}
    OS-->>Customer: Order Details (status: PLACED)
    Note over Customer: UI shows Order PLACED badge
```

---

### 2.2 Scenario 2: Insufficient Stock (Order Rejection)

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Customer (React UI)
    participant OS as Order Service (8081)
    participant ODB as order_db
    participant RMQ as RabbitMQ (smartstock.exchange)
    participant IS as Inventory Service (8082)
    participant IDB as inventory_db

    Customer->>OS: POST /api/orders (productId: 1, qty: 50)
    OS->>ODB: INSERT Order (status: PENDING)
    OS->>RMQ: Publish OrderCreated Event
    OS-->>Customer: 201 Created (status: PENDING)
    
    RMQ->>IS: Deliver OrderCreated
    IS->>IDB: SELECT available_quantity FROM products WHERE id = 1
    Note over IS: Available (10) < Requested (50)
    Note over IS,IDB: Inventory unchanged!
    IS->>RMQ: Publish InventoryRejected Event
    
    RMQ->>OS: Deliver InventoryRejected
    OS->>ODB: UPDATE orders SET status = 'REJECTED' WHERE id = orderId
    
    Customer->>OS: Polling / Status Check
    OS-->>Customer: Order status: REJECTED (Out of Stock)
    Note over Customer: UI shows "Out of Stock" notification.<br/>Rejected order omitted from Customer History.
```

---

### 2.3 Scenario 3: Customer Order Cancellation & Stock Restoration

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Customer (React UI)
    participant OS as Order Service (8081)
    participant ODB as order_db
    participant RMQ as RabbitMQ (smartstock.exchange)
    participant IS as Inventory Service (8082)
    participant IDB as inventory_db

    Customer->>OS: PUT /api/orders/{id}/cancel
    OS->>ODB: Check Order eligible (status == 'PLACED')
    OS->>RMQ: Publish OrderCancelled Event
    OS-->>Customer: 200 OK (Cancellation In-Progress)
    
    RMQ->>IS: Deliver OrderCancelled (order.cancelled.queue)
    Note over IS,IDB: Atomic Stock Restoration
    IS->>IDB: UPDATE products SET available = available + qty, reserved = reserved - qty WHERE reserved >= qty
    IS->>IDB: UPDATE inventory_reservations SET status = 'RELEASED' WHERE order_id = orderId
    IS->>RMQ: Publish StockReleased Event
    
    RMQ->>OS: Deliver StockReleased (inventory.released.queue)
    OS->>ODB: UPDATE orders SET status = 'CANCELLED' WHERE id = orderId
    
    Customer->>OS: GET /api/orders/customer/{name}
    OS-->>Customer: Order Details (status: CANCELLED)
    Note over Customer: UI reflects CANCELLED badge
```

---

## 3. Idempotency & Fault Tolerance

1. **Event Deduplication**:
   - Each event contains an `eventId` (UUID).
   - The Inventory service stores processed event IDs in `processed_events`. If a duplicate `OrderCreated` arrives due to RabbitMQ redelivery, the consumer identifies the existing `eventId` and acknowledges the message without altering stock.

2. **Atomic Invariants**:
   - Total stock $T = A + R$ (Available + Reserved).
   - Available stock $A \ge 0$.
   - Reserved stock $R \ge 0$.
   - When an order is placed: $A \leftarrow A - Q$, $R \leftarrow R + Q$ ($T$ is constant).
   - When an order is cancelled: $A \leftarrow A + Q$, $R \leftarrow R - Q$ ($T$ is constant).
   - Admin adding stock $\Delta S$: $T \leftarrow T + \Delta S$, $A \leftarrow A + \Delta S$.
   - Admin removing stock $\Delta S$: strictly allowed only if $A \ge \Delta S$.
