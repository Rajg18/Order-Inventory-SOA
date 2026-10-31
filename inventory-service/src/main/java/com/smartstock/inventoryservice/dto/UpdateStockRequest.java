package com.smartstock.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class UpdateStockRequest {

    @NotBlank(message = "Action is required")
    @Pattern(regexp = "(?i)^(ADD|REMOVE)$", message = "Action must be ADD or REMOVE")
    private String action;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    public UpdateStockRequest() {
    }

    public UpdateStockRequest(String action, Integer quantity) {
        this.action = action;
        this.quantity = quantity;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
