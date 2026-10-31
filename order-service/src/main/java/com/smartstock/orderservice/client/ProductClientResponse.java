package com.smartstock.orderservice.client;

import java.math.BigDecimal;

public class ProductClientResponse {
    private Long id;
    private String productName;
    private BigDecimal price;
    private String imageUrl;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer reservedQuantity;

    public ProductClientResponse() {
    }

    public ProductClientResponse(Long id, String productName, BigDecimal price, String imageUrl, 
                                 Integer totalQuantity, Integer availableQuantity, Integer reservedQuantity) {
        this.id = id;
        this.productName = productName;
        this.price = price;
        this.imageUrl = imageUrl;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = reservedQuantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }
}
