import React from 'react';
import { ShoppingBag, ShieldCheck, ArrowRight, Layers, Database, Cpu } from 'lucide-react';

export default function RoleSelector({ onSelectRole }) {
  return (
    <div className="role-selector-page">
      <div className="role-selector-hero">
        <div className="system-badge">
          <Layers size={14} /> Service-Oriented Architecture (SOA) Demonstration
        </div>
        <h1 className="hero-title">SmartStock Grocery System</h1>
        <p className="hero-subtitle">
          Autonomous microservices orchestrated via RabbitMQ event choreography & MySQL database-per-service isolation.
        </p>

        <div className="architecture-pills">
          <span className="tech-pill">
            <Cpu size={13} /> Order Service (8081)
          </span>
          <span className="tech-pill">
            <Database size={13} /> Inventory Service (8082)
          </span>
          <span className="tech-pill">
            <Layers size={13} /> RabbitMQ Exchange
          </span>
        </div>
      </div>

      <div className="role-cards-container">
        {/* Customer Role Card */}
        <div className="role-card" onClick={() => onSelectRole('CUSTOMER')}>
          <div className="role-card-icon customer-icon-bg">
            <ShoppingBag size={32} />
          </div>
          <h2 className="role-title">Customer Experience</h2>
          <p className="role-desc">
            Explore 14 quick-commerce grocery items (Fruits, Dairy, Snacks & more). Place orders with real-time asynchronous RabbitMQ stock reservation, view order history, and cancel placed orders.
          </p>
          <ul className="role-features">
            <li>Blinkit-style Grocery Product Grid</li>
            <li>Dynamic Quantity Selector & Live Stock Badge</li>
            <li>Asynchronous Order Placement (OrderCreated)</li>
            <li>Customer Order History & Order Cancellation</li>
          </ul>
          <button className="btn btn-primary btn-block role-cta-btn">
            Enter Customer Dashboard <ArrowRight size={16} />
          </button>
        </div>

        {/* Admin Role Card */}
        <div className="role-card" onClick={() => onSelectRole('ADMIN')}>
          <div className="role-card-icon admin-icon-bg">
            <ShieldCheck size={32} />
          </div>
          <h2 className="role-title">Admin Management</h2>
          <p className="role-desc">
            Directly manage catalog items, adjust stock levels with invariant enforcement (cannot reduce reserved stock), and inspect the Live Customer Orders Slip.
          </p>
          <ul className="role-features">
            <li>Product Catalog Management (Add, Edit, Delete)</li>
            <li>Inventory Stock Adjustment & Out-of-Stock Filter</li>
            <li>Active Reservation Protection (No Deletion/Negative)</li>
            <li>Live Customer Orders Slip (Placed vs Cancelled)</li>
          </ul>
          <button className="btn btn-secondary btn-block role-cta-btn">
            Enter Admin Portal <ArrowRight size={16} />
          </button>
        </div>
      </div>
    </div>
  );
}
