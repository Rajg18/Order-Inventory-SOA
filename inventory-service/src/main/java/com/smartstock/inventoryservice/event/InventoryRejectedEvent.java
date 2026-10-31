package com.smartstock.inventoryservice.event;

public class InventoryRejectedEvent {
    private String eventId;
    private Long orderId;
    private Long productId;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    private String reason;

    public InventoryRejectedEvent() {
    }

    public InventoryRejectedEvent(String eventId, Long orderId, Long productId, Integer requestedQuantity, Integer availableQuantity, String reason) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
        this.reason = reason;
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

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(Integer requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
