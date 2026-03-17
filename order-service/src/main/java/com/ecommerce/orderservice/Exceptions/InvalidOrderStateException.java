package com.ecommerce.orderservice.Exceptions;


public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
}
