package com.smartstock.orderservice.exception;

public class BusinessRuleConflictException extends RuntimeException {
    public BusinessRuleConflictException(String message) {
        super(message);
    }
}
