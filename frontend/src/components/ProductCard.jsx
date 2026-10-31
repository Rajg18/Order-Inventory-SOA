import React, { useState } from 'react';
import { Plus, Minus, ShoppingCart, Loader2 } from 'lucide-react';
import { formatCurrency } from '../utils/formatters';

export default function ProductCard({ product, onOrder, isOrdering }) {
  const [quantity, setQuantity] = useState(1);
  const isOutOfStock = product.availableQuantity <= 0;

  const handleDecrease = () => {
    if (quantity > 1) {
      setQuantity((prev) => prev - 1);
    }
  };

  const handleIncrease = () => {
    if (quantity < product.availableQuantity) {
      setQuantity((prev) => prev + 1);
    }
  };

  const handleOrderClick = () => {
    if (isOutOfStock || isOrdering) return;
    onOrder(product, quantity);
  };

  return (
    <div className={`product-card ${isOutOfStock ? 'product-card-out-of-stock' : ''}`}>
      <div className="product-image-container">
        <img
          src={product.imageUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=600&q=80'}
          alt={product.productName}
          className="product-image"
          loading="lazy"
        />
        {isOutOfStock ? (
          <div className="stock-pill stock-pill-out">OUT OF STOCK</div>
        ) : (
          <div className="stock-pill stock-pill-in">
            {product.availableQuantity} available
          </div>
        )}
      </div>

      <div className="product-info">
        <h3 className="product-title" title={product.productName}>
          {product.productName}
        </h3>

        <div className="product-pricing">
          <span className="product-price">{formatCurrency(product.price)}</span>
          <span className="product-unit">inclusive of all taxes</span>
        </div>

        <div className="product-actions">
          {!isOutOfStock ? (
            <div className="qty-selector">
              <button
                type="button"
                className="qty-btn"
                onClick={handleDecrease}
                disabled={quantity <= 1 || isOrdering}
              >
                <Minus size={14} />
              </button>
              <span className="qty-value">{quantity}</span>
              <button
                type="button"
                className="qty-btn"
                onClick={handleIncrease}
                disabled={quantity >= product.availableQuantity || isOrdering}
              >
                <Plus size={14} />
              </button>
            </div>
          ) : (
            <div className="out-of-stock-label">Currently Unavailable</div>
          )}

          <button
            type="button"
            className="btn btn-order"
            onClick={handleOrderClick}
            disabled={isOutOfStock || isOrdering}
          >
            {isOrdering ? (
              <>
                <Loader2 size={16} className="spinner" />
                <span>Processing...</span>
              </>
            ) : (
              <>
                <ShoppingCart size={16} />
                <span>Order</span>
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
