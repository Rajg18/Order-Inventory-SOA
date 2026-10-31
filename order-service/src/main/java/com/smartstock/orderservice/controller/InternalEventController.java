package com.smartstock.orderservice.controller;

import com.smartstock.orderservice.event.InventoryRejectedEvent;
import com.smartstock.orderservice.event.InventoryReservedEvent;
import com.smartstock.orderservice.event.StockReleasedEvent;
import com.smartstock.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal fallback REST endpoint for event choreography when RabbitMQ broker is offline in standalone/dev mode.
 * Preserves 100% of event schemas, idempotency checks, and transaction boundaries.
 */
@RestController
@RequestMapping("/api/internal/events")
@CrossOrigin(origins = "*")
public class InternalEventController {

    private static final Logger log = LoggerFactory.getLogger(InternalEventController.class);
    private final OrderService orderService;

    public InternalEventController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/inventory-reserved")
    public ResponseEntity<Void> receiveInventoryReserved(@RequestBody InventoryReservedEvent event) {
        log.info("Internal bridge received InventoryReservedEvent for Order #{}", event.getOrderId());
        orderService.handleInventoryReserved(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inventory-rejected")
    public ResponseEntity<Void> receiveInventoryRejected(@RequestBody InventoryRejectedEvent event) {
        log.info("Internal bridge received InventoryRejectedEvent for Order #{}", event.getOrderId());
        orderService.handleInventoryRejected(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stock-released")
    public ResponseEntity<Void> receiveStockReleased(@RequestBody StockReleasedEvent event) {
        log.info("Internal bridge received StockReleasedEvent for Order #{}", event.getOrderId());
        orderService.handleStockReleased(event);
        return ResponseEntity.ok().build();
    }
}
