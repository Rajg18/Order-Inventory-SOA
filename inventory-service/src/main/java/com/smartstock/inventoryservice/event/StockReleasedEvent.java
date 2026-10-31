package com.smartstock.inventoryservice.event;

import java.time.LocalDateTime;

public class StockReleasedEvent {
    private String eventId;
    private Long orderId;
    private Long productId;
    private Integer releasedQuantity;
    private LocalDateTime timestamp;

    public StockReleasedEvent() {
    }

    public StockReleasedEvent(String eventId, Long orderId, Long productId, Integer releasedQuantity, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.productId = productId;
        this.releasedQuantity = releasedQuantity;
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getReleasedQuantity() {
        return releasedQuantity;
    }

    public void setReleasedQuantity(Integer releasedQuantity) {
        this.releasedQuantity = releasedQuantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
