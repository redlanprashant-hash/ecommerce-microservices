package com.ecommerce.orderservice.Exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String> handleOrderNotFound(OrderNotFoundException ex) {



        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());


    }

        @ExceptionHandler(ProductNotFoundException.class)
        public ResponseEntity<String> handleProductNotFound(ProductNotFoundException ex){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        }

        @ExceptionHandler(DownstreamServiceException.class)
        public ResponseEntity<String> handleDownstreamServiceException(DownstreamServiceException ex){
                return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(ex.getMessage());

        }

    @ExceptionHandler(PriceMismatchException.class)
    public ResponseEntity<String> handlePriceMismatch(PriceMismatchException ex){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<String> handleInsufficientStockException(InsufficientStockException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<String> handleInvalidOrderStateException(InvalidOrderStateException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }


    @ExceptionHandler(PaymentServiceException.class)
    public ResponseEntity<String> handlePaymentServiceException(PaymentServiceException ex){
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ex.getMessage());
    }

    }


