package com.smartstock.orderservice.event;

import java.time.LocalDateTime;

public class InventoryReservedEvent {
    private String eventId;
    private Long orderId;
    private Long productId;
    private Integer reservedQuantity;
    private LocalDateTime timestamp;

    public InventoryReservedEvent() {
    }

    public InventoryReservedEvent(String eventId, Long orderId, Long productId, Integer reservedQuantity, LocalDateTime timestamp) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.productId = productId;
        this.reservedQuantity = reservedQuantity;
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

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
