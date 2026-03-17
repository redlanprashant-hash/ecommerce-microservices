package com.ecommerce.orderservice.Exceptions;

public class PaymentServiceException extends RuntimeException{

    public PaymentServiceException(String message){
        super(message);
    }

}
