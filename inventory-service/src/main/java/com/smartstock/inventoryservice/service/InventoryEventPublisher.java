package com.smartstock.inventoryservice.service;

import com.smartstock.inventoryservice.config.RabbitMQConfig;
import com.smartstock.inventoryservice.event.InventoryRejectedEvent;
import com.smartstock.inventoryservice.event.InventoryReservedEvent;
import com.smartstock.inventoryservice.event.StockReleasedEvent;
import com.smartstock.inventoryservice.model.ServiceEventLog;
import com.smartstock.inventoryservice.repository.ServiceEventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class InventoryEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ServiceEventLogRepository eventLogRepository;
    private final RestClient orderServiceRestClient;

    public InventoryEventPublisher(RabbitTemplate rabbitTemplate, ServiceEventLogRepository eventLogRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventLogRepository = eventLogRepository;
        this.orderServiceRestClient = RestClient.builder()
                .baseUrl("http://localhost:8081")
                .build();
    }

    public String publishInventoryReserved(Long orderId, Long productId, Integer reservedQuantity) {
        String eventId = UUID.randomUUID().toString();
        InventoryReservedEvent event = new InventoryReservedEvent(eventId, orderId, productId, reservedQuantity, LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_INVENTORY_RESERVED,
                    event
            );
            log.info("Published InventoryReserved event [eventId={}, orderId={}, productId={}, qty={}]",
                    eventId, orderId, productId, reservedQuantity);

            eventLogRepository.save(new ServiceEventLog(
                    eventId,
                    "InventoryReserved",
                    "PUBLISHED",
                    orderId,
                    productId,
                    "Stock reserved (" + reservedQuantity + " units). Published InventoryReserved to exchange '" + 
                    RabbitMQConfig.EXCHANGE_NAME + "' (RoutingKey: " + RabbitMQConfig.ROUTING_KEY_INVENTORY_RESERVED + ")"
            ));
        } catch (Exception e) {
            log.warn("RabbitMQ publish failed for InventoryReserved event {}: {}. Triggering direct fallback dispatch...", eventId, e.getMessage());
            eventLogRepository.save(new ServiceEventLog(
                    eventId,
                    "InventoryReserved",
                    "FALLBACK_DISPATCHED",
                    orderId,
                    productId,
                    "Dispatched InventoryReserved via fallback bridge (" + reservedQuantity + " units reserved)"
            ));
            dispatchToOrderService("/api/internal/events/inventory-reserved", event);
        }

        return eventId;
    }

    public String publishInventoryRejected(Long orderId, Long productId, Integer requestedQuantity, Integer availableQuantity, String reason) {
        String eventId = UUID.randomUUID().toString();
        InventoryRejectedEvent event = new InventoryRejectedEvent(eventId, orderId, productId, requestedQuantity, availableQuantity, reason);

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_INVENTORY_REJECTED,
                    event
            );
            log.info("Published InventoryRejected event [eventId={}, orderId={}, productId={}, requested={}, available={}, reason={}]",
                    eventId, orderId, productId, requestedQuantity, availableQuantity, reason);

            eventLogRepository.save(new ServiceEventLog(
                    eventId,
                    "InventoryRejected",
                    "PUBLISHED",
                    orderId,
                    productId,
                    "Insufficient stock! Requested " + requestedQuantity + ", available " + availableQuantity + 
                    ". Reason: " + reason + ". Published InventoryRejected to exchange '" + RabbitMQConfig.EXCHANGE_NAME + "'"
            ));
        } catch (Exception e) {
            log.warn("RabbitMQ publish failed for InventoryRejected event {}: {}. Triggering direct fallback dispatch...", eventId, e.getMessage());
            eventLogRepository.save(new ServiceEventLog(
                    eventId,
                    "InventoryRejected",
                    "FALLBACK_DISPATCHED",
                    orderId,
                    productId,
                    "Dispatched InventoryRejected via fallback bridge (Reason: " + reason + ")"
            ));
            dispatchToOrderService("/api/internal/events/inventory-rejected", event);
        }

        return eventId;
    }

    public String publishStockReleased(Long orderId, Long productId, Integer releasedQuantity) {
        String eventId = UUID.randomUUID().toString();
        StockReleasedEvent event = new StockReleasedEvent(eventId, orderId, productId, releasedQuantity, LocalDateTime.now());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_INVENTORY_RELEASED,
                    event
            );
            log.info("Published StockReleased event [eventId={}, orderId={}, productId={}, qty={}]",
                    eventId, orderId, productId, releasedQuantity);

            eventLogRepository.save(new ServiceEventLog(
                    eventId,
                    "StockReleased",
                    "PUBLISHED",
                    orderId,
                    productId,
                    "Reserved stock restored (" + releasedQuantity + " units). Published StockReleased to exchange '" + 
                    RabbitMQConfig.EXCHANGE_NAME + "' (RoutingKey: " + RabbitMQConfig.ROUTING_KEY_INVENTORY_RELEASED + ")"
            ));
        } catch (Exception e) {
            log.warn("RabbitMQ publish failed for StockReleased event {}: {}. Triggering direct fallback dispatch...", eventId, e.getMessage());
            eventLogRepository.save(new ServiceEventLog(
                    eventId,
                    "StockReleased",
                    "FALLBACK_DISPATCHED",
                    orderId,
                    productId,
                    "Dispatched StockReleased via fallback bridge (" + releasedQuantity + " units released)"
            ));
            dispatchToOrderService("/api/internal/events/stock-released", event);
        }

        return eventId;
    }

    private void dispatchToOrderService(String uri, Object body) {
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(80);
                orderServiceRestClient.post()
                        .uri(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception ex) {
                log.error("Failed to dispatch event to Order Service fallback bridge {}: {}", uri, ex.getMessage());
            }
        });
    }
}
