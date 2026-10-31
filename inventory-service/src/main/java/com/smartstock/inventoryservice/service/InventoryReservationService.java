package com.smartstock.inventoryservice.service;

import com.smartstock.inventoryservice.event.OrderCancelledEvent;
import com.smartstock.inventoryservice.event.OrderCreatedEvent;
import com.smartstock.inventoryservice.model.*;
import com.smartstock.inventoryservice.repository.InventoryReservationRepository;
import com.smartstock.inventoryservice.repository.ProcessedEventRepository;
import com.smartstock.inventoryservice.repository.ProductRepository;
import com.smartstock.inventoryservice.repository.ServiceEventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InventoryReservationService {

    private static final Logger log = LoggerFactory.getLogger(InventoryReservationService.class);

    private final ProductRepository productRepository;
    private final InventoryReservationRepository reservationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ServiceEventLogRepository eventLogRepository;
    private final InventoryEventPublisher eventPublisher;

    public InventoryReservationService(ProductRepository productRepository,
                                       InventoryReservationRepository reservationRepository,
                                       ProcessedEventRepository processedEventRepository,
                                       ServiceEventLogRepository eventLogRepository,
                                       InventoryEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.reservationRepository = reservationRepository;
        this.processedEventRepository = processedEventRepository;
        this.eventLogRepository = eventLogRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreated event: [eventId={}, orderId={}, productId={}, qty={}]",
                event.getEventId(), event.getOrderId(), event.getProductId(), event.getQuantity());

        // Idempotency check: ignore duplicate events
        if (event.getEventId() != null && processedEventRepository.existsById(event.getEventId())) {
            log.warn("Duplicate OrderCreated event detected (eventId={}). Discarding.", event.getEventId());
            return;
        }

        eventLogRepository.save(new ServiceEventLog(
                event.getEventId(),
                "OrderCreated",
                "RECEIVED",
                event.getOrderId(),
                event.getProductId(),
                "Inventory Service received OrderCreated for Order #" + event.getOrderId() + 
                ", requesting " + event.getQuantity() + " units."
        ));

        // Attempt atomic conditional stock reservation:
        // UPDATE products SET available = available - qty, reserved = reserved + qty WHERE id = ? AND available >= qty
        int rowsUpdated = productRepository.reserveStock(event.getProductId(), event.getQuantity());

        if (rowsUpdated > 0) {
            log.info("Stock reserved successfully for Order #{} (Product ID: {}, Qty: {})",
                    event.getOrderId(), event.getProductId(), event.getQuantity());

            // Save reservation record
            InventoryReservation reservation = new InventoryReservation(
                    event.getOrderId(),
                    event.getProductId(),
                    event.getQuantity(),
                    ReservationStatus.RESERVED
            );
            reservationRepository.save(reservation);

            // Record processed event for deduplication
            if (event.getEventId() != null) {
                processedEventRepository.save(new ProcessedEvent(event.getEventId(), "OrderCreated"));
            }

            eventLogRepository.save(new ServiceEventLog(
                    event.getEventId(),
                    "StockReserved",
                    "INTERNAL",
                    event.getOrderId(),
                    event.getProductId(),
                    "Reserved " + event.getQuantity() + " units for Order #" + event.getOrderId()
            ));

            // Publish InventoryReserved event
            eventPublisher.publishInventoryReserved(event.getOrderId(), event.getProductId(), event.getQuantity());

        } else {
            // Insufficient stock or product missing: inventory remains completely unchanged!
            Optional<Product> productOpt = productRepository.findById(event.getProductId());
            int available = productOpt.map(Product::getAvailableQuantity).orElse(0);

            log.warn("Stock reservation FAILED for Order #{}: Requested {} units, but available is {}",
                    event.getOrderId(), event.getQuantity(), available);

            if (event.getEventId() != null) {
                processedEventRepository.save(new ProcessedEvent(event.getEventId(), "OrderCreated_Rejected"));
            }

            eventLogRepository.save(new ServiceEventLog(
                    event.getEventId(),
                    "ReservationRejected",
                    "INTERNAL",
                    event.getOrderId(),
                    event.getProductId(),
                    "Insufficient stock for Order #" + event.getOrderId() + 
                    " (Requested: " + event.getQuantity() + ", Available: " + available + "). Inventory untouched."
            ));

            // Publish InventoryRejected event
            eventPublisher.publishInventoryRejected(
                    event.getOrderId(),
                    event.getProductId(),
                    event.getQuantity(),
                    available,
                    "Insufficient stock (Available: " + available + ", Requested: " + event.getQuantity() + ")"
            );
        }
    }

    @Transactional
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("Received OrderCancelled event: [eventId={}, orderId={}, productId={}, qty={}]",
                event.getEventId(), event.getOrderId(), event.getProductId(), event.getQuantity());

        // Idempotency check: prevent duplicate stock restoration
        if (event.getEventId() != null && processedEventRepository.existsById(event.getEventId())) {
            log.warn("Duplicate OrderCancelled event detected (eventId={}). Discarding.", event.getEventId());
            return;
        }

        eventLogRepository.save(new ServiceEventLog(
                event.getEventId(),
                "OrderCancelled",
                "RECEIVED",
                event.getOrderId(),
                event.getProductId(),
                "Inventory Service received OrderCancelled for Order #" + event.getOrderId() + ". Restoring stock."
        ));

        // Atomic conditional stock release:
        // UPDATE products SET available = available + qty, reserved = reserved - qty WHERE id = ? AND reserved >= qty
        int rowsUpdated = productRepository.releaseStock(event.getProductId(), event.getQuantity());

        // Update reservation status to RELEASED if present
        Optional<InventoryReservation> reservationOpt = reservationRepository.findByOrderId(event.getOrderId());
        reservationOpt.ifPresent(reservation -> {
            reservation.setReservationStatus(ReservationStatus.RELEASED);
            reservationRepository.save(reservation);
        });

        if (event.getEventId() != null) {
            processedEventRepository.save(new ProcessedEvent(event.getEventId(), "OrderCancelled"));
        }

        eventLogRepository.save(new ServiceEventLog(
                event.getEventId(),
                "StockRestored",
                "INTERNAL",
                event.getOrderId(),
                event.getProductId(),
                "Restored " + event.getQuantity() + " units for Order #" + event.getOrderId() + " back to available stock."
        ));

        // Publish StockReleased event
        eventPublisher.publishStockReleased(event.getOrderId(), event.getProductId(), event.getQuantity());
    }
}
