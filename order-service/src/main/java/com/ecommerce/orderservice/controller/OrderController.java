package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.PlaceOrderRequest;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;


    public OrderController(OrderService orderService) {
        this.orderService = orderService;

    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,HttpServletRequest httpServletRequest) {

        String userIdFromHeader = httpServletRequest.getHeader("X-User-Id");
        if(userIdFromHeader == null){
            logger.info("X-User-Id received from Gateway: null");
            throw new RuntimeException("Missing user identity");
        }
        logger.info("X-User-Id received from Gateway: {}", userIdFromHeader);

        Long userId = Long.valueOf(userIdFromHeader);
        logger.info("Placing order for userId={}", userId);


        // 2️. Pass userId to service
        OrderResponse response = orderService.placeOrder(request,userId);

        return new ResponseEntity<>(response, HttpStatus.CREATED);


    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderNumber(order.getOrderNumber());
        response.setProductId(order.getProductId());
        response.setQuantity(order.getQuantity());
        response.setPrice(order.getPrice());
        response.setStatus(order.getStatus());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByOrderNumber(
            @PathVariable String orderNumber) {

        OrderResponse response = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrderByUserId(@PathVariable Long userId) {
        logger.info("From controller calling getOrderByUserId with userId {}", userId);
        List<OrderResponse> response = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/{orderId}/pay")
    public ResponseEntity<String> payForOrder(@PathVariable Long orderId, HttpServletRequest request){

        String userIdHeader = request.getHeader("X-User-Id");

        if(userIdHeader == null){
            throw new RuntimeException("Missing User Identity");
        }

        Long userId = Long.valueOf(userIdHeader);
        orderService.payForOrder(orderId, userId);

        return ResponseEntity.ok("Payement Successfull");



    }

}
