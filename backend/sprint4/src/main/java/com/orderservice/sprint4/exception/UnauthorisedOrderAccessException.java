package com.orderservice.sprint4.exception;

public class UnauthorisedOrderAccessException extends RuntimeException {
    public UnauthorisedOrderAccessException(String message) {
        super(message);
    }
}
