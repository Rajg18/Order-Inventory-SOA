package com.smartstock.orderservice.repository;

import com.smartstock.orderservice.model.Order;
import com.smartstock.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByCustomerNameOrderByCreatedAtDesc(String customerName);
    List<Order> findByCustomerNameAndStatusInOrderByCreatedAtDesc(String customerName, List<OrderStatus> statuses);
}
