package com.smartstock.inventoryservice.controller;

import com.smartstock.inventoryservice.event.OrderCancelledEvent;
import com.smartstock.inventoryservice.event.OrderCreatedEvent;
import com.smartstock.inventoryservice.service.InventoryReservationService;
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
    private final InventoryReservationService reservationService;

    public InternalEventController(InventoryReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/order-created")
    public ResponseEntity<Void> receiveOrderCreated(@RequestBody OrderCreatedEvent event) {
        log.info("Internal bridge received OrderCreatedEvent: [orderId={}, productId={}, qty={}]",
                event.getOrderId(), event.getProductId(), event.getQuantity());
        reservationService.handleOrderCreated(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/order-cancelled")
    public ResponseEntity<Void> receiveOrderCancelled(@RequestBody OrderCancelledEvent event) {
        log.info("Internal bridge received OrderCancelledEvent: [orderId={}, productId={}, qty={}]",
                event.getOrderId(), event.getProductId(), event.getQuantity());
        reservationService.handleOrderCancelled(event);
        return ResponseEntity.ok().build();
    }
}
