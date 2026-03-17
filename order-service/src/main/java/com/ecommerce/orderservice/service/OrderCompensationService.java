package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.model.Order;
import org.springframework.stereotype.Service;


public interface OrderCompensationService {

    void cancelOrder(Long orderId);
}
