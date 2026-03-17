package com.ecommerce.orderservice.Exceptions;

public class DownstreamServiceException extends RuntimeException {

    public DownstreamServiceException(String message) {
        super(message);
    }

}
