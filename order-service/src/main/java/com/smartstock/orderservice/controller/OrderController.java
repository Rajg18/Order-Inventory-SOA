package com.smartstock.orderservice.controller;

import com.smartstock.orderservice.dto.CancelOrderResponse;
import com.smartstock.orderservice.dto.CreateOrderRequest;
import com.smartstock.orderservice.dto.OrderResponse;
import com.smartstock.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/customer/{customerName}")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(@PathVariable String customerName) {
        return ResponseEntity.ok(orderService.getCustomerOrders(customerName));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<CancelOrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
