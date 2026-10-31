import React, { useState } from 'react';
import { Receipt, RefreshCw, CheckCircle2, Ban, AlertCircle, User } from 'lucide-react';
import { formatCurrency, formatDateTime } from '../utils/formatters';

export default function AdminOrdersSlip({ orders, onRefresh, isRefreshing }) {
  const [statusFilter, setStatusFilter] = useState('ALL');

  const filteredOrders = orders.filter((order) => {
    if (statusFilter === 'PLACED') return order.status === 'PLACED';
    if (statusFilter === 'CANCELLED') return order.status === 'CANCELLED';
    return true;
  });

  const placedCount = orders.filter((o) => o.status === 'PLACED').length;
  const cancelledCount = orders.filter((o) => o.status === 'CANCELLED').length;

  return (
    <div className="card orders-slip-card">
      <div className="card-header">
        <div className="flex-row items-center gap-3">
          <div className="slip-icon-container">
            <Receipt className="slip-icon" />
          </div>
          <div>
            <h2 className="card-title">Live Customer Orders Slip</h2>
            <p className="card-subtitle">
              Real-time audit slip of orders placed by customers. Clearly flags cancellations and stock restoration.
            </p>
          </div>
        </div>

        <div className="flex-row items-center gap-2">
          <div className="btn-group-pill">
            <button
              className={`pill-btn ${statusFilter === 'ALL' ? 'active' : ''}`}
              onClick={() => setStatusFilter('ALL')}
            >
              All ({orders.length})
            </button>
            <button
              className={`pill-btn ${statusFilter === 'PLACED' ? 'active text-success' : ''}`}
              onClick={() => setStatusFilter('PLACED')}
            >
              Placed ({placedCount})
            </button>
            <button
              className={`pill-btn ${statusFilter === 'CANCELLED' ? 'active text-danger' : ''}`}
              onClick={() => setStatusFilter('CANCELLED')}
            >
              Cancelled ({cancelledCount})
            </button>
          </div>

          <button
            className="btn btn-outline btn-sm"
            onClick={onRefresh}
            disabled={isRefreshing}
            title="Refresh Orders Slip"
          >
            <RefreshCw size={14} className={isRefreshing ? 'spinner' : ''} />
            <span>Refresh</span>
          </button>
        </div>
      </div>

      {filteredOrders.length === 0 ? (
        <div className="empty-orders-card">
          <Receipt size={36} className="empty-icon" />
          <p className="text-muted">No orders matching the selected filter.</p>
        </div>
      ) : (
        <div className="slip-receipt-container">
          <div className="table-responsive">
            <table className="data-table slip-table">
              <thead>
                <tr>
                  <th>Slip # / Order ID</th>
                  <th>Customer Name</th>
                  <th>Product Purchased (Snapshot)</th>
                  <th>Qty</th>
                  <th>Unit Price</th>
                  <th>Total Amount</th>
                  <th>Order Status</th>
                  <th>Order Placed At</th>
                </tr>
              </thead>
              <tbody>
                {filteredOrders.map((order) => {
                  const isPlaced = order.status === 'PLACED';
                  const isCancelled = order.status === 'CANCELLED';

                  return (
                    <tr
                      key={order.id}
                      className={isCancelled ? 'row-cancelled' : isPlaced ? 'row-placed' : ''}
                    >
                      <td className="font-mono font-bold">#{order.id}</td>
                      <td>
                        <div className="flex-row items-center gap-1">
                          <User size={13} className="text-muted" />
                          <span className="font-semibold">{order.customerName}</span>
                        </div>
                      </td>
                      <td className="font-medium">{order.productNameSnapshot}</td>
                      <td>
                        <span className="badge badge-neutral">{order.quantity}</span>
                      </td>
                      <td>{formatCurrency(order.unitPriceSnapshot)}</td>
                      <td className="font-semibold text-primary font-mono">
                        {formatCurrency(order.totalAmount)}
                      </td>
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
                        {!isPlaced && !isCancelled && (
                          <span className="badge badge-warning">{order.status}</span>
                        )}
                      </td>
                      <td className="text-muted font-sm">{formatDateTime(order.createdAt)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
