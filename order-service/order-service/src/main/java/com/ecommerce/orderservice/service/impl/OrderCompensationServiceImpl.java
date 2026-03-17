package com.ecommerce.orderservice.service.impl;

import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderCompensationService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.stereotype.Service;


@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class OrderCompensationServiceImpl implements OrderCompensationService {

    private final OrderRepository orderRepository;

    public OrderCompensationServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void cancelOrder(Long orderId) {

        orderRepository.updateStatus(orderId, OrderStatus.CANCELLED);

    }
}
