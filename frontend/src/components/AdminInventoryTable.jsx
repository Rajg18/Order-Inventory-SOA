import React, { useState } from 'react';
import { PlusCircle, MinusCircle, AlertTriangle, CheckCircle, Package } from 'lucide-react';

export default function AdminInventoryTable({ products, onUpdateStock }) {
  const [filterOutOfStock, setFilterOutOfStock] = useState(false);
  const [activeStockModal, setActiveStockModal] = useState(null); // { product, action: 'ADD'|'REMOVE' }
  const [stockAmount, setStockAmount] = useState(5);
  const [actionError, setActionError] = useState('');
  const [isUpdating, setIsUpdating] = useState(false);

  const filteredProducts = filterOutOfStock
    ? products.filter((p) => p.availableQuantity <= 0)
    : products;

  const handleOpenStockModal = (product, action) => {
    setActiveStockModal({ product, action });
    setStockAmount(5);
    setActionError('');
  };

  const handleStockSubmit = async (e) => {
    e.preventDefault();
    if (!activeStockModal || stockAmount <= 0) return;

    const { product, action } = activeStockModal;

    if (action === 'REMOVE' && stockAmount > product.availableQuantity) {
      setActionError(`Cannot remove ${stockAmount} units. Maximum available to remove is ${product.availableQuantity} (Reserved: ${product.reservedQuantity}).`);
      return;
    }

    try {
      setIsUpdating(true);
      setActionError('');
      await onUpdateStock(product.id, action, stockAmount);
      setActiveStockModal(null);
    } catch (err) {
      setActionError(err.response?.data?.message || err.message || 'Stock modification failed');
    } finally {
      setIsUpdating(false);
    }
  };

  return (
    <div className="card">
      <div className="card-header">
        <div>
          <h2 className="card-title">Inventory & Stock Control</h2>
          <p className="card-subtitle">
            Enforces strict SOA invariants: Available stock cannot be negative. Reserved stock cannot be removed.
          </p>
        </div>
        <div className="filter-group">
          <button
            className={`btn btn-sm ${filterOutOfStock ? 'btn-danger' : 'btn-outline'}`}
            onClick={() => setFilterOutOfStock(!filterOutOfStock)}
          >
            <AlertTriangle size={14} />
            <span>{filterOutOfStock ? 'Showing Out of Stock' : 'Filter Out of Stock'}</span>
          </button>
        </div>
      </div>

      <div className="table-responsive">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Product Name</th>
              <th>Total Stock</th>
              <th>Available Stock</th>
              <th>Reserved Stock</th>
              <th>Status</th>
              <th className="text-right">Stock Operations</th>
            </tr>
          </thead>
          <tbody>
            {filteredProducts.map((p) => {
              const isOut = p.availableQuantity <= 0;
              return (
                <tr key={p.id}>
                  <td className="font-mono font-semibold">#{p.id}</td>
                  <td className="font-medium">{p.productName}</td>
                  <td>{p.totalQuantity}</td>
                  <td>
                    <span className={`font-semibold ${isOut ? 'text-danger' : 'text-success'}`}>
                      {p.availableQuantity}
                    </span>
                  </td>
                  <td>
                    <span className="font-semibold text-warning">{p.reservedQuantity}</span>
                  </td>
                  <td>
                    {isOut ? (
                      <span className="badge badge-danger">OUT OF STOCK</span>
                    ) : (
                      <span className="badge badge-success">IN STOCK</span>
                    )}
                  </td>
                  <td className="text-right">
                    <div className="table-actions">
                      <button
                        className="btn btn-outline btn-xs"
                        onClick={() => handleOpenStockModal(p, 'ADD')}
                        title="Add inventory"
                      >
                        <PlusCircle size={14} /> Add Stock
                      </button>
                      <button
                        className="btn btn-outline btn-xs"
                        onClick={() => handleOpenStockModal(p, 'REMOVE')}
                        disabled={p.availableQuantity <= 0}
                        title="Remove available inventory"
                      >
                        <MinusCircle size={14} /> Remove Stock
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {activeStockModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <h3 className="modal-title">
              {activeStockModal.action === 'ADD' ? 'Add Stock' : 'Remove Stock'}: {activeStockModal.product.productName}
            </h3>
            <p className="font-sm text-muted mb-4">
              Current Available: <strong>{activeStockModal.product.availableQuantity}</strong> | Active Reserved: <strong>{activeStockModal.product.reservedQuantity}</strong>
            </p>

            {actionError && (
              <div className="alert alert-danger font-sm">
                <AlertTriangle size={15} />
                <span>{actionError}</span>
              </div>
            )}

            <form onSubmit={handleStockSubmit} className="form-stack">
              <div className="form-group">
                <label className="form-label">
                  Units to {activeStockModal.action === 'ADD' ? 'Add' : 'Remove'}
                </label>
                <input
                  type="number"
                  min="1"
                  max={activeStockModal.action === 'REMOVE' ? activeStockModal.product.availableQuantity : 9999}
                  className="form-input"
                  value={stockAmount}
                  onChange={(e) => setStockAmount(parseInt(e.target.value, 10) || 1)}
                  required
                />
              </div>

              <div className="quick-btn-group">
                {[5, 10, 20, 50].map((qty) => (
                  <button
                    key={qty}
                    type="button"
                    className="btn btn-secondary btn-xs"
                    onClick={() => setStockAmount(qty)}
                    disabled={activeStockModal.action === 'REMOVE' && qty > activeStockModal.product.availableQuantity}
                  >
                    +{qty}
                  </button>
                ))}
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn btn-outline"
                  onClick={() => setActiveStockModal(null)}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className={`btn ${activeStockModal.action === 'ADD' ? 'btn-primary' : 'btn-danger'}`}
                  disabled={isUpdating}
                >
                  {isUpdating ? 'Updating...' : `Confirm ${activeStockModal.action === 'ADD' ? 'Addition' : 'Removal'}`}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
