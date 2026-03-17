package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.PlaceOrderRequest;
import com.ecommerce.orderservice.model.Order;
import org.springframework.http.ResponseEntity;

import java.util.List;

    public interface OrderService {

        OrderResponse placeOrder(PlaceOrderRequest request,Long userId);

        OrderResponse getOrderByOrderNumber(String orderNumber);

        List<OrderResponse> getOrdersByUserId(Long userId);

        void payForOrder(Long orderId, Long userId);
    }

