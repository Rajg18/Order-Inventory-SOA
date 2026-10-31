package com.smartstock.orderservice.dto;

import com.smartstock.orderservice.model.OrderStatus;

public class CancelOrderResponse {
    private Long id;
    private OrderStatus status;
    private String message;

    public CancelOrderResponse() {
    }

    public CancelOrderResponse(Long id, OrderStatus status, String message) {
        this.id = id;
        this.status = status;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
