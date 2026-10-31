# SmartStock: SOA-Based Order & Inventory Management System

SmartStock is a Service-Oriented Architecture (SOA) application demonstrating independent service boundaries, database ownership, asynchronous event choreography using **RabbitMQ**, synchronous REST communication, and atomic inventory management.

---

## 1. System Topology

| Component | Port | Technology | Database / Broker |
|---|---|---|---|
| **Frontend** | `5173` | React 18, Vite, Axios | N/A |
| **Order Service** | `8081` | Spring Boot 3.3, Java 17 | `order_db` (MySQL) |
| **Inventory Service** | `8082` | Spring Boot 3.3, Java 17 | `inventory_db` (MySQL) |
| **Message Broker** | `5672` / `15672` | RabbitMQ 3.13 (Management) | `smartstock.exchange` |
| **Database Server** | `3306` | MySQL 8.0 | `order_db`, `inventory_db` |

---

## 2. Key Features

- **Demonstration Role Selector**: Switch seamlessly between **Customer** and **Admin** views.
- **Customer Dashboard (Blinkit-Style Quick Commerce)**:
  - 14 seeded grocery items spanning Fruits & Veggies, Dairy, Beverages, Snacks, Packaged Food, Household Essentials, and Personal Care.
  - Live available stock indicator, dynamic quantity selector, instant order placement.
  - Clear Customer-visible statuses: **`PLACED`** and **`CANCELLED`** (out-of-stock orders handled gracefully without polluting order history).
  - Cancellation capability that automatically restores reserved stock in the inventory service.
- **Admin Dashboard**:
  - **Product Management**: Add new products, update prices and details, delete products (guarded against active reservations).
  - **Inventory Management**: View stock, add/remove stock, prevent negative quantities, ensure reserved stock cannot be improperly removed.
  - **Live Customer Orders Slip**: Real-time overview of customer orders, highlighting current status (`PLACED` or `CANCELLED`).
- **Educational Monitors**:
  - **API Activity Monitor**: Live Axios interceptor capturing HTTP Method, Endpoint, Status Code, Duration, Request/Response JSON payloads with expand & clear tools.
  - **Service Event Monitor**: Real-time display of RabbitMQ events (`OrderCreated`, `InventoryReserved`, `InventoryRejected`, `OrderCancelled`, `StockReleased`).

---

## 3. Quick Start Guide

### Step 1: Start Infrastructure (Docker Compose)
```bash
cd infrastructure
docker-compose up -d
```
*RabbitMQ Management UI: [http://localhost:15672](http://localhost:15672) (user: `guest`, pass: `guest`)*

### Step 2: Start Inventory Service (Port 8082)
```bash
cd inventory-service
mvn spring-boot:run
```

### Step 3: Start Order Service (Port 8081)
```bash
cd order-service
mvn spring-boot:run
```

### Step 4: Start Frontend (Port 5173)
```bash
cd frontend
npm install
npm run dev
```

Visit [http://localhost:5173](http://localhost:5173) in your browser.

---

## 4. Documentation Directory

- [Architecture & SOA Principles](docs/architecture.md)
- [REST API Contracts](docs/api-contracts.md)
- [Service Interactions & Event Workflows](docs/service-interaction.md)
