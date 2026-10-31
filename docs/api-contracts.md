# SmartStock REST API Contracts Specification

## 1. Overview
This document formalizes the HTTP service contracts between clients (e.g. React Frontend) and the two backend services:
- **Order Management Service** (Base URL: `http://localhost:8081`)
- **Inventory Management Service** (Base URL: `http://localhost:8082`)

All request and response bodies use JSON (`Content-Type: application/json`). Standard HTTP status codes are strictly observed.

---

## 2. Order Management Service Contracts (Port 8081)

### 2.1 POST /api/orders
Creates an order request, records a pending snapshot, and publishes `OrderCreated` to RabbitMQ.

- **Request Body**:
```json
{
  "customerName": "Alice Johnson",
  "productId": 1,
  "productName": "Fresh Alphonso Mangoes (1kg)",
  "unitPrice": 249.00,
  "quantity": 2
}
```

- **Validation Rules**:
  - `customerName`: Required, non-empty.
  - `productId`: Required, positive integer.
  - `quantity`: Required, minimum 1.
  - `unitPrice`: Required, non-negative decimal.

- **Response `201 Created`**:
```json
{
  "id": 101,
  "customerName": "Alice Johnson",
  "productId": 1,
  "productNameSnapshot": "Fresh Alphonso Mangoes (1kg)",
  "quantity": 2,
  "unitPriceSnapshot": 249.00,
  "totalAmount": 498.00,
  "status": "PLACED",
  "createdAt": "2026-10-31T19:30:00Z",
  "updatedAt": "2026-10-31T19:30:01Z"
}
```

- **Error Responses**:
  - `400 Bad Request`: Validation failure.
  - `409 Conflict`: Insufficient stock / inventory rejected.

---

### 2.2 GET /api/orders
Retrieves all orders in reverse chronological order (used by Admin Live Order Slip).

- **Response `200 OK`**:
```json
[
  {
    "id": 101,
    "customerName": "Alice Johnson",
    "productId": 1,
    "productNameSnapshot": "Fresh Alphonso Mangoes (1kg)",
    "quantity": 2,
    "unitPriceSnapshot": 249.00,
    "totalAmount": 498.00,
    "status": "PLACED",
    "createdAt": "2026-10-31T19:30:00Z"
  }
]
```

---

### 2.3 GET /api/orders/{id}
Retrieves a specific order by ID.

- **Response `200 OK`**: Order JSON.
- **Error `404 Not Found`**: Order ID does not exist.

---

### 2.4 GET /api/orders/customer/{customerName}
Retrieves order history filtered for a specific customer.
Only orders with status `PLACED` or `CANCELLED` are returned.

- **Response `200 OK`**: Array of customer orders.

---

### 2.5 PUT /api/orders/{id}/cancel
Requests cancellation of a `PLACED` order, triggering an `OrderCancelled` event to release inventory.

- **Response `200 OK`**:
```json
{
  "id": 101,
  "status": "CANCELLED",
  "message": "Order 101 cancellation initiated and stock released successfully."
}
```

- **Error Responses**:
  - `400 Bad Request`: Order is already `CANCELLED`.
  - `404 Not Found`: Order does not exist.

---

### 2.6 GET /api/orders/events
Audit log of RabbitMQ events published and received by the Order Service.

- **Response `200 OK`**:
```json
[
  {
    "id": 1,
    "eventId": "b78b5e2a-7a54-47b1-9128-4fbb26c368f1",
    "eventType": "OrderCreated",
    "direction": "PUBLISHED",
    "orderId": 101,
    "payload": "{\"productId\":1,\"quantity\":2}",
    "timestamp": "2026-10-31T19:30:00Z"
  }
]
```

---

## 3. Inventory Management Service Contracts (Port 8082)

### 3.1 GET /api/products
Retrieves all products in the catalog.

- **Response `200 OK`**:
```json
[
  {
    "id": 1,
    "productName": "Fresh Alphonso Mangoes (1kg)",
    "price": 249.00,
    "imageUrl": "https://images.unsplash.com/...",
    "totalQuantity": 25,
    "availableQuantity": 23,
    "reservedQuantity": 2
  }
]
```

---

### 3.2 GET /api/products/{id}
Retrieves single product details.

- **Response `200 OK`**: Product JSON.
- **Error `404 Not Found`**: Product does not exist.

---

### 3.3 POST /api/products
Creates a new product (Admin function).

- **Request Body**:
```json
{
  "productName": "Greek Yogurt Blueberry (150g)",
  "price": 60.00,
  "imageUrl": "https://images.unsplash.com/...",
  "totalQuantity": 30
}
```

- **Response `201 Created`**: Product JSON (`availableQuantity` defaults to `totalQuantity`, `reservedQuantity` to 0).

---

### 3.4 PUT /api/products/{id}
Updates product name, price, and image URL (Admin function).

- **Request Body**:
```json
{
  "productName": "Greek Yogurt Blueberry (150g)",
  "price": 65.00,
  "imageUrl": "https://images.unsplash.com/..."
}
```

- **Response `200 OK`**: Updated Product JSON.

---

### 3.5 PUT /api/products/{id}/stock
Adds or removes stock (Admin function).

- **Request Body**:
```json
{
  "action": "ADD", 
  "quantity": 10
}
```
*(Or action: "REMOVE")*

- **Rules**:
  - `availableQuantity` can never fall below 0.
  - Cannot remove stock that is currently reserved (`availableQuantity = totalQuantity - reservedQuantity`).

- **Response `200 OK`**: Updated Product JSON.
- **Error `409 Conflict`**: Insufficient available stock to remove requested amount.

---

### 3.6 GET /api/products/{id}/availability
Returns current stock availability status.

- **Response `200 OK`**:
```json
{
  "productId": 1,
  "availableQuantity": 23,
  "inStock": true
}
```

---

### 3.7 DELETE /api/products/{id}
Deletes a product, strictly validated against active reservations.

- **Response `200 OK`**: `{ "message": "Product 1 deleted successfully." }`
- **Error `409 Conflict`**: `"Cannot delete product with active reservations."`

---

### 3.8 GET /api/products/events
Audit log of RabbitMQ events published and received by the Inventory Service.

- **Response `200 OK`**: Array of event log items.
