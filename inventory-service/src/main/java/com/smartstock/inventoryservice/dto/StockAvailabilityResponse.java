package com.smartstock.inventoryservice.dto;

public class StockAvailabilityResponse {
    private Long productId;
    private Integer availableQuantity;
    private boolean inStock;

    public StockAvailabilityResponse() {
    }

    public StockAvailabilityResponse(Long productId, Integer availableQuantity, boolean inStock) {
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
