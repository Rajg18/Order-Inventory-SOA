package com.smartstock.orderservice.service;

import com.smartstock.orderservice.client.InventoryClient;
import com.smartstock.orderservice.client.ProductClientResponse;
import com.smartstock.orderservice.dto.CancelOrderResponse;
import com.smartstock.orderservice.dto.CreateOrderRequest;
import com.smartstock.orderservice.dto.OrderResponse;
import com.smartstock.orderservice.event.InventoryRejectedEvent;
import com.smartstock.orderservice.event.InventoryReservedEvent;
import com.smartstock.orderservice.event.StockReleasedEvent;
import com.smartstock.orderservice.exception.BusinessRuleConflictException;
import com.smartstock.orderservice.exception.ResourceNotFoundException;
import com.smartstock.orderservice.model.Order;
import com.smartstock.orderservice.model.OrderStatus;
import com.smartstock.orderservice.model.ServiceEventLog;
import com.smartstock.orderservice.repository.OrderRepository;
import com.smartstock.orderservice.repository.ServiceEventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ServiceEventLogRepository eventLogRepository;
    private final InventoryClient inventoryClient;
    private final OrderEventPublisher eventPublisher;

    public OrderService(OrderRepository orderRepository,
                        ServiceEventLogRepository eventLogRepository,
                        InventoryClient inventoryClient,
                        OrderEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventLogRepository = eventLogRepository;
        this.inventoryClient = inventoryClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String productName = request.getProductName().trim();
        BigDecimal unitPrice = request.getUnitPrice();

        // Synchronous REST check: Validate product with Inventory Service
        try {
            ProductClientResponse productResponse = inventoryClient.getProductById(request.getProductId());
            if (productResponse != null) {
                if (productResponse.getProductName() != null && !productResponse.getProductName().isBlank()) {
                    productName = productResponse.getProductName();
                }
                if (productResponse.getPrice() != null) {
                    unitPrice = productResponse.getPrice();
                }
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not synchronously verify product ID {} with Inventory Service: {}. Falling back to request snapshot.",
                    request.getProductId(), e.getMessage());
        }

        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = new Order(
                request.getCustomerName().trim(),
                request.getProductId(),
                productName,
                request.getQuantity(),
                unitPrice,
                total,
                OrderStatus.PENDING
        );

        Order saved = orderRepository.saveAndFlush(order);

        // Publish OrderCreated event to RabbitMQ
        eventPublisher.publishOrderCreated(saved.getId(), saved.getProductId(), saved.getQuantity());

        return new OrderResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return new OrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(String customerName) {
        // Customer panel: ONLY show PLACED and CANCELLED orders
        List<OrderStatus> visibleStatuses = List.of(OrderStatus.PLACED, OrderStatus.CANCELLED);
        return orderRepository.findByCustomerNameAndStatusInOrderByCreatedAtDesc(customerName.trim(), visibleStatuses)
                .stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public CancelOrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessRuleConflictException("Order #" + id + " is already CANCELLED.");
        }

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new BusinessRuleConflictException("Only PLACED orders can be cancelled. Current status: " + order.getStatus());
        }

        // Publish OrderCancelled event to RabbitMQ to release stock in Inventory Service
        eventPublisher.publishOrderCancelled(order.getId(), order.getProductId(), order.getQuantity());

        // Optimistically set to CANCELLED or wait for event confirmation
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return new CancelOrderResponse(order.getId(), order.getStatus(), "Order #" + id + " cancellation initiated.");
    }

    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Processing InventoryReserved event for Order #{}", event.getOrderId());
        Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(OrderStatus.PLACED);
            orderRepository.save(order);

            eventLogRepository.save(new ServiceEventLog(
                    event.getEventId(),
                    "InventoryReserved",
                    "RECEIVED",
                    order.getId(),
                    order.getProductId(),
                    "Order #" + order.getId() + " confirmed. Status updated to PLACED (Reserved " + 
                    event.getReservedQuantity() + " units)."
            ));
        } else {
            log.warn("Order #{} not found for InventoryReserved event", event.getOrderId());
        }
    }

    @Transactional
    public void handleInventoryRejected(InventoryRejectedEvent event) {
        log.warn("Processing InventoryRejected event for Order #{}: {}", event.getOrderId(), event.getReason());
        Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(OrderStatus.REJECTED);
            orderRepository.save(order);

            eventLogRepository.save(new ServiceEventLog(
                    event.getEventId(),
                    "InventoryRejected",
                    "RECEIVED",
                    order.getId(),
                    order.getProductId(),
                    "Order #" + order.getId() + " rejected due to insufficient stock: " + event.getReason()
            ));
        }
    }

    @Transactional
    public void handleStockReleased(StockReleasedEvent event) {
        log.info("Processing StockReleased event for Order #{}", event.getOrderId());
        Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);

            eventLogRepository.save(new ServiceEventLog(
                    event.getEventId(),
                    "StockReleased",
                    "RECEIVED",
                    order.getId(),
                    order.getProductId(),
                    "Stock restoration confirmed (" + event.getReleasedQuantity() + " units) for Order #" + order.getId() + 
                    ". Status confirmed as CANCELLED."
            ));
        }
    }
}
