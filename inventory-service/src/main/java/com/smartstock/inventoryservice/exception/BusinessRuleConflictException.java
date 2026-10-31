package com.smartstock.inventoryservice.exception;

public class BusinessRuleConflictException extends RuntimeException {
    public BusinessRuleConflictException(String message) {
        super(message);
    }
}
