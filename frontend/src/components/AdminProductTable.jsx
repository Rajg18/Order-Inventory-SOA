import React, { useState } from 'react';
import { Plus, Trash2, Edit2, AlertCircle, CheckCircle } from 'lucide-react';
import { formatCurrency } from '../utils/formatters';

export default function AdminProductTable({ products, onCreateProduct, onUpdateProduct, onDeleteProduct }) {
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [formData, setFormData] = useState({
    productName: '',
    price: '',
    imageUrl: '',
    totalQuantity: 20,
  });
  const [errorMsg, setErrorMsg] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const resetForm = () => {
    setFormData({
      productName: '',
      price: '',
      imageUrl: '',
      totalQuantity: 20,
    });
    setErrorMsg('');
    setEditingProduct(null);
  };

  const handleOpenAdd = () => {
    resetForm();
    setShowAddModal(true);
  };

  const handleOpenEdit = (product) => {
    setEditingProduct(product);
    setFormData({
      productName: product.productName,
      price: product.price,
      imageUrl: product.imageUrl || '',
      totalQuantity: product.totalQuantity,
    });
    setErrorMsg('');
    setShowAddModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.productName.trim() || !formData.price) {
      setErrorMsg('Product name and price are required.');
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMsg('');

      if (editingProduct) {
        await onUpdateProduct(editingProduct.id, {
          productName: formData.productName.trim(),
          price: parseFloat(formData.price),
          imageUrl: formData.imageUrl.trim(),
        });
      } else {
        await onCreateProduct({
          productName: formData.productName.trim(),
          price: parseFloat(formData.price),
          imageUrl: formData.imageUrl.trim() || 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=600&q=80',
          totalQuantity: parseInt(formData.totalQuantity, 10),
        });
      }
      setShowAddModal(false);
      resetForm();
    } catch (err) {
      setErrorMsg(err.response?.data?.message || err.message || 'Operation failed');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id, name, reservedQty) => {
    if (reservedQty > 0) {
      alert(`Cannot delete '${name}' because it currently has ${reservedQty} active reserved unit(s).`);
      return;
    }
    if (window.confirm(`Are you sure you want to delete '${name}'? This will remove it from the customer panel.`)) {
      try {
        await onDeleteProduct(id);
      } catch (err) {
        alert(err.response?.data?.message || 'Delete operation failed due to active reservations.');
      }
    }
  };

  return (
    <div className="card">
      <div className="card-header">
        <div>
          <h2 className="card-title">Product Catalog Management</h2>
          <p className="card-subtitle">
            Inventory Service is the sole source of truth for products. Edits never corrupt historical orders.
          </p>
        </div>
        <button className="btn btn-primary" onClick={handleOpenAdd}>
          <Plus size={16} /> Add Product
        </button>
      </div>

      <div className="table-responsive">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Preview</th>
              <th>Product Name</th>
              <th>Price</th>
              <th>Total Stock</th>
              <th>Available</th>
              <th>Reserved</th>
              <th className="text-right">Actions</th>
            </tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id}>
                <td className="font-mono font-semibold">#{p.id}</td>
                <td>
                  <img
                    src={p.imageUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=600&q=80'}
                    alt={p.productName}
                    className="table-thumbnail"
                  />
                </td>
                <td className="font-medium">{p.productName}</td>
                <td className="font-semibold">{formatCurrency(p.price)}</td>
                <td>{p.totalQuantity}</td>
                <td>
                  <span className={`badge ${p.availableQuantity > 0 ? 'badge-success' : 'badge-danger'}`}>
                    {p.availableQuantity}
                  </span>
                </td>
                <td>
                  <span className={`badge ${p.reservedQuantity > 0 ? 'badge-warning' : 'badge-neutral'}`}>
                    {p.reservedQuantity}
                  </span>
                </td>
                <td className="text-right">
                  <div className="table-actions">
                    <button
                      className="btn btn-icon"
                      onClick={() => handleOpenEdit(p)}
                      title="Edit product details & price"
                    >
                      <Edit2 size={15} />
                    </button>
                    <button
                      className="btn btn-icon btn-icon-danger"
                      onClick={() => handleDelete(p.id, p.productName, p.reservedQuantity)}
                      title="Delete product"
                      disabled={p.reservedQuantity > 0}
                    >
                      <Trash2 size={15} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showAddModal && (
        <div className="modal-backdrop">
          <div className="modal-card">
            <h3 className="modal-title">
              {editingProduct ? 'Edit Product Details' : 'Add New Product'}
            </h3>

            {errorMsg && (
              <div className="alert alert-danger font-sm">
                <AlertCircle size={15} />
                <span>{errorMsg}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="form-stack">
              <div className="form-group">
                <label className="form-label">Product Name</label>
                <input
                  type="text"
                  className="form-input"
                  value={formData.productName}
                  onChange={(e) => setFormData({ ...formData, productName: e.target.value })}
                  placeholder="e.g. Organic Strawberries (250g)"
                  required
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Price (₹)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0.01"
                    className="form-input"
                    value={formData.price}
                    onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                    placeholder="199.00"
                    required
                  />
                </div>

                {!editingProduct && (
                  <div className="form-group">
                    <label className="form-label">Initial Quantity</label>
                    <input
                      type="number"
                      min="0"
                      className="form-input"
                      value={formData.totalQuantity}
                      onChange={(e) => setFormData({ ...formData, totalQuantity: e.target.value })}
                      required
                    />
                  </div>
                )}
              </div>

              <div className="form-group">
                <label className="form-label">Image URL (Unsplash or CDN)</label>
                <input
                  type="url"
                  className="form-input"
                  value={formData.imageUrl}
                  onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                  placeholder="https://images.unsplash.com/..."
                />
              </div>

              <div className="modal-actions">
                <button
                  type="button"
                  className="btn btn-outline"
                  onClick={() => setShowAddModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                  {isSubmitting ? 'Saving...' : editingProduct ? 'Update Product' : 'Create Product'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
