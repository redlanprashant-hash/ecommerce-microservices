package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {


    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUserId(Long userId);


    @Modifying
    @Query("update Order o set o.status = :status where o.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status")OrderStatus status);

}
