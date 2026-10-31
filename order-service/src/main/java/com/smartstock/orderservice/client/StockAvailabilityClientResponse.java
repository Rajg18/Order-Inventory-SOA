package com.smartstock.orderservice.client;

public class StockAvailabilityClientResponse {
    private Long productId;
    private Integer availableQuantity;
    private boolean inStock;

    public StockAvailabilityClientResponse() {
    }

    public StockAvailabilityClientResponse(Long productId, Integer availableQuantity, boolean inStock) {
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.inStock = inStock;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }
}
