import React, { useState, useEffect } from 'react';
import AdminProductTable from '../components/AdminProductTable';
import AdminInventoryTable from '../components/AdminInventoryTable';
import AdminOrdersSlip from '../components/AdminOrdersSlip';
import EducationalMonitorsSection from '../components/EducationalMonitorsSection';
import { inventoryService } from '../services/inventoryService';
import { orderService } from '../services/orderService';
import { Package, Layers, AlertTriangle, Receipt, RefreshCw, CheckCircle, AlertCircle } from 'lucide-react';

export default function AdminDashboard() {
  const [activeTab, setActiveTab] = useState('PRODUCTS'); // 'PRODUCTS', 'INVENTORY', 'ORDERS_SLIP'
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [notification, setNotification] = useState(null);

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => {
      setNotification(null);
    }, 4000);
  };

  const loadData = async () => {
    try {
      const [prodData, orderData] = await Promise.all([
        inventoryService.getProducts(),
        orderService.getAllOrders(),
      ]);
      setProducts(prodData);
      setOrders(orderData);
    } catch (err) {
      console.error('Failed to load admin data:', err);
    } finally {
      setLoading(false);
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    loadData();

    // Polling every 4 seconds to sync inventory stock & orders
    const interval = setInterval(() => {
      loadData();
    }, 4000);

    return () => clearInterval(interval);
  }, []);

  const handleManualRefresh = () => {
    setIsRefreshing(true);
    loadData();
  };

  const handleCreateProduct = async (productData) => {
    const created = await inventoryService.createProduct(productData);
    showNotification('success', `Product '${created.productName}' created successfully.`);
    loadData();
  };

  const handleUpdateProduct = async (id, productData) => {
    const updated = await inventoryService.updateProduct(id, productData);
    showNotification('success', `Product #${id} updated successfully.`);
    loadData();
  };

  const handleDeleteProduct = async (id) => {
    await inventoryService.deleteProduct(id);
    showNotification('success', `Product #${id} deleted from catalog.`);
    loadData();
  };

  const handleUpdateStock = async (id, action, quantity) => {
    const updated = await inventoryService.updateStock(id, action, quantity);
    showNotification('success', `Stock updated for '${updated.productName}'. New available: ${updated.availableQuantity}`);
    loadData();
  };

  // Stats calculation
  const totalProducts = products.length;
  const outOfStockCount = products.filter((p) => p.availableQuantity <= 0).length;
  const totalReservedUnits = products.reduce((sum, p) => sum + (p.reservedQuantity || 0), 0);
  const totalPlacedOrders = orders.filter((o) => o.status === 'PLACED').length;
  const totalCancelledOrders = orders.filter((o) => o.status === 'CANCELLED').length;

  return (
    <div className="admin-dashboard">
      {/* Banner */}
      <div className="dashboard-banner admin-banner">
        <div className="banner-content">
          <div className="flex-row items-center gap-2">
            <span className="badge badge-admin">Administrator Portal</span>
            <span className="banner-tag">Inventory Authority & Order Audits</span>
          </div>
          <h1 className="banner-title">Operations & Catalog Control</h1>
          <p className="banner-subtitle">
            Manage product metadata, enforce stock integrity rules, and observe live customer orders and cancellations.
          </p>
        </div>

        <button
          className="btn btn-outline btn-sm bg-white"
          onClick={handleManualRefresh}
          disabled={isRefreshing}
        >
          <RefreshCw size={14} className={isRefreshing ? 'spinner' : ''} />
          <span>Refresh Backend State</span>
        </button>
      </div>

      {/* Notification Toast */}
      {notification && (
        <div className={`toast-notification toast-${notification.type}`}>
          {notification.type === 'success' ? <CheckCircle size={18} /> : <AlertCircle size={18} />}
          <span>{notification.message}</span>
        </div>
      )}

      {/* KPI Stats Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon-wrapper stat-primary">
            <Package size={22} />
          </div>
          <div>
            <div className="stat-value">{totalProducts}</div>
            <div className="stat-label">Total Catalog Products</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper stat-danger">
            <AlertTriangle size={22} />
          </div>
          <div>
            <div className="stat-value">{outOfStockCount}</div>
            <div className="stat-label">Out of Stock Items</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper stat-warning">
            <Layers size={22} />
          </div>
          <div>
            <div className="stat-value">{totalReservedUnits}</div>
            <div className="stat-label">Active Reserved Units</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper stat-success">
            <Receipt size={22} />
          </div>
          <div>
            <div className="stat-value">{totalPlacedOrders} <span className="stat-sub">({totalCancelledOrders} cancelled)</span></div>
            <div className="stat-label">Customer Orders Placed</div>
          </div>
        </div>
      </div>

      {/* Navigation Tabs */}
      <div className="admin-tabs">
        <button
          className={`tab-btn ${activeTab === 'PRODUCTS' ? 'active' : ''}`}
          onClick={() => setActiveTab('PRODUCTS')}
        >
          <Package size={16} />
          <span>Product Catalog ({products.length})</span>
        </button>
        <button
          className={`tab-btn ${activeTab === 'INVENTORY' ? 'active' : ''}`}
          onClick={() => setActiveTab('INVENTORY')}
        >
          <Layers size={16} />
          <span>Inventory & Stock Control</span>
        </button>
        <button
          className={`tab-btn ${activeTab === 'ORDERS_SLIP' ? 'active' : ''}`}
          onClick={() => setActiveTab('ORDERS_SLIP')}
        >
          <Receipt size={16} />
          <span>Live Customer Orders Slip ({orders.length})</span>
        </button>
      </div>

      {/* Tab Panels */}
      <div className="tab-content">
        {activeTab === 'PRODUCTS' && (
          <AdminProductTable
            products={products}
            onCreateProduct={handleCreateProduct}
            onUpdateProduct={handleUpdateProduct}
            onDeleteProduct={handleDeleteProduct}
          />
        )}

        {activeTab === 'INVENTORY' && (
          <AdminInventoryTable
            products={products}
            onUpdateStock={handleUpdateStock}
          />
        )}

        {activeTab === 'ORDERS_SLIP' && (
          <AdminOrdersSlip
            orders={orders}
            onRefresh={handleManualRefresh}
            isRefreshing={isRefreshing}
          />
        )}
      </div>

      {/* Mandatory Educational Monitors */}
      <div className="monitors-section mt-4">
        <EducationalMonitorsSection />
      </div>
    </div>
  );
}
