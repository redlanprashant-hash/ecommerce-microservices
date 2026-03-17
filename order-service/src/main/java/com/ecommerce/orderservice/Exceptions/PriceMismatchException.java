package com.ecommerce.orderservice.Exceptions;

public class PriceMismatchException extends RuntimeException{

    public PriceMismatchException(String message){
        super(message);
    }

}
