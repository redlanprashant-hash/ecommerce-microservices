package com.ecommerce.orderservice.service.impl;

import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderPersistenceService;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

@Service
public class OrderPersistenceServiceImpl implements OrderPersistenceService {

    private final OrderRepository orderRepository;

    public OrderPersistenceServiceImpl(OrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }
}
