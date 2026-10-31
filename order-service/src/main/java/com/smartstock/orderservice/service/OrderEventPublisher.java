package com.smartstock.orderservice.service;

import com.smartstock.orderservice.config.RabbitMQConfig;
import com.smartstock.orderservice.event.OrderCancelledEvent;
import com.smartstock.orderservice.event.OrderCreatedEvent;
import com.smartstock.orderservice.model.ServiceEventLog;
import com.smartstock.orderservice.repository.ServiceEventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ServiceEventLogRepository eventLogRepository;
    private final RestClient inventoryServiceRestClient;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate, ServiceEventLogRepository eventLogRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventLogRepository = eventLogRepository;
        this.inventoryServiceRestClient = RestClient.builder()
                .baseUrl("http://localhost:8082")
                .build();
    }

    public String publishOrderCreated(Long orderId, Long productId, Integer quantity) {
        String eventId = UUID.randomUUID().toString();
        OrderCreatedEvent event = new OrderCreatedEvent(eventId, orderId, productId, quantity, LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_ORDER_CREATED,
                    event
            );
            log.info("Published OrderCreated event [eventId={}, orderId={}, productId={}, qty={}]",
                    eventId, orderId, productId, quantity);

            eventLogRepository.save(new ServiceEventLog(
                    eventId,
                    "OrderCreated",
                    "PUBLISHED",
                    orderId,
                    productId,
                    "Published OrderCreated to RabbitMQ exchange '" + RabbitMQConfig.EXCHANGE_NAME + "' (RoutingKey: " + RabbitMQConfig.ROUTING_KEY_ORDER_CREATED + ")"
            ));
        } catch (Exception e) {
            log.warn("RabbitMQ publish failed for OrderCreated event {}: {}. Triggering direct fallback dispatch...", eventId, e.getMessage());
            eventLogRepository.save(new ServiceEventLog(
                    eventId,
                    "OrderCreated",
                    "FALLBACK_DISPATCHED",
                    orderId,
                    productId,
                    "Dispatched OrderCreated via fallback bridge (Product ID: " + productId + ", Qty: " + quantity + ")"
            ));
            dispatchToInventoryService("/api/internal/events/order-created", event);
        }

        return eventId;
    }

    public String publishOrderCancelled(Long orderId, Long productId, Integer quantity) {
        String eventId = UUID.randomUUID().toString();
        OrderCancelledEvent event = new OrderCancelledEvent(eventId, orderId, productId, quantity, LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_ORDER_CANCELLED,
                    event
            );
            log.info("Published OrderCancelled event [eventId={}, orderId={}, productId={}, qty={}]",
                    eventId, orderId, productId, quantity);

            eventLogRepository.save(new ServiceEventLog(
                    eventId,
                    "OrderCancelled",
                    "PUBLISHED",
                    orderId,
                    productId,
                    "Published OrderCancelled to RabbitMQ exchange '" + RabbitMQConfig.EXCHANGE_NAME + "' (RoutingKey: " + RabbitMQConfig.ROUTING_KEY_ORDER_CANCELLED + ")"
            ));
        } catch (Exception e) {
            log.warn("RabbitMQ publish failed for OrderCancelled event {}: {}. Triggering direct fallback dispatch...", eventId, e.getMessage());
            eventLogRepository.save(new ServiceEventLog(
                    eventId,
                    "OrderCancelled",
                    "FALLBACK_DISPATCHED",
                    orderId,
                    productId,
                    "Dispatched OrderCancelled via fallback bridge (Order ID: " + orderId + ", Qty: " + quantity + ")"
            ));
            dispatchToInventoryService("/api/internal/events/order-cancelled", event);
        }

        return eventId;
    }

    private void dispatchToInventoryService(String uri, Object body) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(80);
                inventoryServiceRestClient.post()
                        .uri(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception ex) {
                log.error("Failed to dispatch event to Inventory Service fallback bridge {}: {}", uri, ex.getMessage());
            }
        });
    }
}
