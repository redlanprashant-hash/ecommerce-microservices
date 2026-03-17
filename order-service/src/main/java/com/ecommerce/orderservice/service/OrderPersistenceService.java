package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.model.Order;

public interface OrderPersistenceService {

    public Order saveOrder(Order order);

}
