import React from 'react';
import { Ban, CheckCircle2, Clock, PackageCheck } from 'lucide-react';
import { formatCurrency, formatDateTime } from '../utils/formatters';

export default function CustomerOrderHistory({ orders, onCancelOrder, cancellingOrderId }) {
  if (!orders || orders.length === 0) {
    return (
      <div className="card empty-orders-card">
        <PackageCheck size={40} className="empty-icon" />
        <h3>No Orders Placed Yet</h3>
        <p>Choose your quick-commerce groceries above and click Order to test asynchronous reservation.</p>
      </div>
    );
  }

  return (
    <div className="card order-history-card">
      <div className="card-header">
        <div>
          <h2 className="card-title">My Order History</h2>
          <p className="card-subtitle">
            Synchronized with Order Service (Port 8081). Cancellations release reserved stock back via RabbitMQ.
          </p>
        </div>
        <span className="badge badge-neutral">{orders.length} orders</span>
      </div>

      <div className="table-responsive">
        <table className="data-table">
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Product Snapshot</th>
              <th>Unit Price</th>
              <th>Quantity</th>
              <th>Total Amount</th>
              <th>Status</th>
              <th>Placed At</th>
              <th className="text-right">Action</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => {
              const isPlaced = order.status === 'PLACED';
              const isCancelled = order.status === 'CANCELLED';
              const isCancelling = cancellingOrderId === order.id;

              return (
                <tr key={order.id}>
                  <td className="font-mono font-semibold">#{order.id}</td>
                  <td className="font-medium">{order.productNameSnapshot}</td>
                  <td>{formatCurrency(order.unitPriceSnapshot)}</td>
                  <td>{order.quantity}</td>
                  <td className="font-semibold">{formatCurrency(order.totalAmount)}</td>
                  <td>
                    {isPlaced && (
                      <span className="badge badge-success">
                        <CheckCircle2 size={12} /> PLACED
                      </span>
                    )}
                    {isCancelled && (
                      <span className="badge badge-danger">
                        <Ban size={12} /> CANCELLED
                      </span>
                    )}
                  </td>
                  <td className="text-muted font-sm">{formatDateTime(order.createdAt)}</td>
                  <td className="text-right">
                    {isPlaced && (
                      <button
                        className="btn btn-danger btn-sm"
                        onClick={() => onCancelOrder(order.id)}
                        disabled={isCancelling}
                        title="Cancel this order and release reserved stock"
                      >
                        {isCancelling ? 'Cancelling...' : 'Cancel Order'}
                      </button>
                    )}
                    {isCancelled && (
                      <span className="text-muted font-sm font-italic">Stock Restored</span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
