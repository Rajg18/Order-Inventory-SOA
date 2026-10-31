import React, { useState, useEffect, useRef } from 'react';
import ProductCard from '../components/ProductCard';
import CustomerOrderHistory from '../components/CustomerOrderHistory';
import EducationalMonitorsSection from '../components/EducationalMonitorsSection';
import { inventoryService } from '../services/inventoryService';
import { orderService } from '../services/orderService';
import { User, AlertCircle, CheckCircle, RefreshCw, ShoppingCart } from 'lucide-react';

const CATEGORIES = [
  'All',
  'Fruits & Vegetables',
  'Dairy & Bread',
  'Beverages',
  'Snacks & Munchies',
  'Packaged Food',
  'Household & Cleaning',
  'Personal Care',
];

export default function CustomerDashboard() {
  const [customerName, setCustomerName] = useState('Alice Johnson');
  const [products, setProducts] = useState([]);
  const [customerOrders, setCustomerOrders] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [orderingProductId, setOrderingProductId] = useState(null);
  const [cancellingOrderId, setCancellingOrderId] = useState(null);
  const [notification, setNotification] = useState(null); // { type: 'success'|'error', message }

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => {
      setNotification(null);
    }, 5000);
  };

  const fetchProducts = async () => {
    try {
      const data = await inventoryService.getProducts();
      setProducts(data);
    } catch (err) {
      console.error('Failed to fetch products:', err);
    } finally {
      setLoadingProducts(false);
    }
  };

  const fetchCustomerOrders = async () => {
    if (!customerName.trim()) return;
    try {
      const orders = await orderService.getCustomerOrders(customerName);
      setCustomerOrders(orders);
    } catch (err) {
      console.error('Failed to fetch customer orders:', err);
    }
  };

  useEffect(() => {
    fetchProducts();
    fetchCustomerOrders();

    // Lightweight polling every 4 seconds to reflect updated stock and order status
    const interval = setInterval(() => {
      fetchProducts();
      fetchCustomerOrders();
    }, 4000);

    return () => clearInterval(interval);
  }, [customerName]);

  const handleOrder = async (product, quantity) => {
    try {
      setOrderingProductId(product.id);
      showNotification('info', `Sending order for ${quantity}x ${product.productName}...`);

      const orderPayload = {
        customerName: customerName.trim(),
        productId: product.id,
        productName: product.productName,
        unitPrice: product.price,
        quantity: quantity,
      };

      const createdOrder = await orderService.createOrder(orderPayload);

      // Give RabbitMQ a brief moment (600ms) to choreograph reservation, then poll immediate status
      setTimeout(async () => {
        try {
          const latestOrder = await orderService.getOrderById(createdOrder.id);
          if (latestOrder.status === 'PLACED') {
            showNotification('success', `Order #${latestOrder.id} confirmed and PLACED! Stock reserved successfully.`);
          } else if (latestOrder.status === 'REJECTED') {
            showNotification('error', `Out of Stock: Unable to place order #${latestOrder.id}. Insufficient stock.`);
          } else {
            showNotification('success', `Order #${createdOrder.id} submitted for processing.`);
          }
          fetchProducts();
          fetchCustomerOrders();
        } catch {
          fetchProducts();
          fetchCustomerOrders();
        } finally {
          setOrderingProductId(null);
        }
      }, 700);

    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Order placement failed.';
      showNotification('error', msg);
      setOrderingProductId(null);
    }
  };

  const handleCancelOrder = async (orderId) => {
    try {
      setCancellingOrderId(orderId);
      await orderService.cancelOrder(orderId);
      showNotification('success', `Order #${orderId} cancelled. Reserved stock released back to inventory.`);

      // Refresh data
      setTimeout(() => {
        fetchProducts();
        fetchCustomerOrders();
        setCancellingOrderId(null);
      }, 500);
    } catch (err) {
      const msg = err.response?.data?.message || err.message || 'Cancellation failed.';
      showNotification('error', msg);
      setCancellingOrderId(null);
    }
  };

  // Simple category matching
  const filteredProducts = products.filter((p) => {
    if (selectedCategory === 'All') return true;
    const name = p.productName.toLowerCase();
    if (selectedCategory === 'Fruits & Vegetables') return name.includes('mango') || name.includes('broccoli');
    if (selectedCategory === 'Dairy & Bread') return name.includes('milk') || name.includes('paneer') || name.includes('yogurt');
    if (selectedCategory === 'Beverages') return name.includes('coca') || name.includes('coconut');
    if (selectedCategory === 'Snacks & Munchies') return name.includes('doritos') || name.includes('bhujia');
    if (selectedCategory === 'Packaged Food') return name.includes('maggi') || name.includes('muesli');
    if (selectedCategory === 'Household & Cleaning') return name.includes('detergent') || name.includes('dishwash');
    if (selectedCategory === 'Personal Care') return name.includes('sanitizer') || name.includes('toothpaste');
    return true;
  });

  return (
    <div className="customer-dashboard">
      {/* Top Banner / Customer Info */}
      <div className="dashboard-banner">
        <div className="banner-content">
          <div className="flex-row items-center gap-2">
            <span className="badge badge-customer">Quick Commerce</span>
            <span className="banner-tag">Instant Delivery Simulation</span>
          </div>
          <h1 className="banner-title">Grocery & Essentials</h1>
          <p className="banner-subtitle">
            Order products below to trigger RabbitMQ asynchronous stock reservation in the Inventory Service.
          </p>
        </div>

        <div className="customer-identity-box">
          <label className="font-xs font-semibold text-muted">SHOPPING AS</label>
          <div className="customer-select-wrapper">
            <User size={16} className="text-primary" />
            <select
              className="customer-select"
              value={customerName}
              onChange={(e) => setCustomerName(e.target.value)}
            >
              <option value="Alice Johnson">Alice Johnson</option>
              <option value="Bob Smith">Bob Smith</option>
              <option value="Charlie Brown">Charlie Brown</option>
              <option value="Dana Scully">Dana Scully</option>
            </select>
          </div>
        </div>
      </div>

      {/* Notifications */}
      {notification && (
        <div className={`toast-notification toast-${notification.type}`}>
          {notification.type === 'success' && <CheckCircle size={18} />}
          {notification.type === 'error' && <AlertCircle size={18} />}
          {notification.type === 'info' && <RefreshCw size={18} className="spinner" />}
          <span>{notification.message}</span>
        </div>
      )}

      {/* Category Pills */}
      <div className="category-scroll-container">
        {CATEGORIES.map((cat) => (
          <button
            key={cat}
            className={`category-pill ${selectedCategory === cat ? 'active' : ''}`}
            onClick={() => setSelectedCategory(cat)}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Product Grid */}
      <div className="catalog-section">
        <div className="section-header">
          <h2 className="section-title">
            {selectedCategory === 'All' ? 'All Products' : selectedCategory} ({filteredProducts.length})
          </h2>
          <span className="text-muted font-sm">
            Live prices & quantities retrieved from Inventory Service (8082)
          </span>
        </div>

        {loadingProducts ? (
          <div className="loading-state">
            <RefreshCw size={32} className="spinner text-primary" />
            <p>Loading quick-commerce grocery catalogue...</p>
          </div>
        ) : filteredProducts.length === 0 ? (
          <div className="empty-orders-card">
            <ShoppingCart size={40} className="empty-icon" />
            <p>No products found in this category.</p>
          </div>
        ) : (
          <div className="product-grid">
            {filteredProducts.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onOrder={handleOrder}
                isOrdering={orderingProductId === product.id}
              />
            ))}
          </div>
        )}
      </div>

      {/* Customer Order History */}
      <div className="order-history-section">
        <CustomerOrderHistory
          orders={customerOrders}
          onCancelOrder={handleCancelOrder}
          cancellingOrderId={cancellingOrderId}
        />
      </div>

      {/* Mandatory Educational Monitors */}
      <div className="monitors-section">
        <EducationalMonitorsSection />
      </div>
    </div>
  );
}
