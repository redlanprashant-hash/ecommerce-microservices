package com.ecommerce.authservice.exception;

public class InvalidCredentialException extends RuntimeException{

    public InvalidCredentialException(String message){
        super(message);
    }

}
