package com.smartstock.inventoryservice;

import com.smartstock.inventoryservice.event.OrderCancelledEvent;
import com.smartstock.inventoryservice.event.OrderCreatedEvent;
import com.smartstock.inventoryservice.model.InventoryReservation;
import com.smartstock.inventoryservice.model.Product;
import com.smartstock.inventoryservice.model.ReservationStatus;
import com.smartstock.inventoryservice.repository.InventoryReservationRepository;
import com.smartstock.inventoryservice.repository.ProcessedEventRepository;
import com.smartstock.inventoryservice.repository.ProductRepository;
import com.smartstock.inventoryservice.repository.ServiceEventLogRepository;
import com.smartstock.inventoryservice.service.InventoryEventPublisher;
import com.smartstock.inventoryservice.service.InventoryReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ConcurrencyAndIdempotencyTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryReservationRepository reservationRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @Mock
    private ServiceEventLogRepository eventLogRepository;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private InventoryReservationService reservationService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConcurrentOrdersForLastAvailableUnits() throws InterruptedException {
        int threads = 10;
        int initialStock = 3;
        AtomicInteger remainingStock = new AtomicInteger(initialStock);
        AtomicInteger successfulReservations = new AtomicInteger(0);
        AtomicInteger rejectedReservations = new AtomicInteger(0);

        // Simulate atomic database reservation behavior:
        // UPDATE products SET available = available - 1 WHERE id = 1 AND available >= 1
        when(productRepository.reserveStock(eq(1L), eq(1))).thenAnswer(invocation -> {
            int current = remainingStock.get();
            while (current > 0) {
                if (remainingStock.compareAndSet(current, current - 1)) {
                    successfulReservations.incrementAndGet();
                    return 1; // 1 row updated
                }
                current = remainingStock.get();
            }
            rejectedReservations.incrementAndGet();
            return 0; // 0 rows updated (insufficient stock)
        });

        when(productRepository.findById(1L)).thenAnswer(inv ->
                Optional.of(new Product(1L, "Mangoes", BigDecimal.valueOf(249.00), "img.jpg", 10, remainingStock.get(), 0))
        );

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            final long orderId = 1000L + i;
            final String eventId = "concurrent-evt-" + i;
            executor.submit(() -> {
                try {
                    OrderCreatedEvent event = new OrderCreatedEvent(eventId, orderId, 1L, 1, LocalDateTime.now());
                    reservationService.handleOrderCreated(event);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Verification of concurrency guarantees:
        // Exactly 3 reservations must succeed, exactly 7 must be rejected, and remaining stock must be 0
        assertEquals(3, successfulReservations.get(), "Exactly 3 reservations should succeed");
        assertEquals(7, rejectedReservations.get(), "Remaining 7 requests should be rejected");
        assertEquals(0, remainingStock.get(), "Available stock should never drop below 0");
        verify(eventPublisher, times(3)).publishInventoryReserved(anyLong(), eq(1L), eq(1));
        verify(eventPublisher, times(7)).publishInventoryRejected(anyLong(), eq(1L), eq(1), eq(0), anyString());
    }

    @Test
    public void testDuplicateDeliveryIdempotency() {
        String eventId = "duplicate-uuid-1234";
        OrderCreatedEvent event = new OrderCreatedEvent(eventId, 501L, 1L, 2, LocalDateTime.now());

        // First delivery: not yet processed
        when(processedEventRepository.existsById(eventId)).thenReturn(false);
        when(productRepository.reserveStock(1L, 2)).thenReturn(1);

        reservationService.handleOrderCreated(event);

        // Verify first delivery reserved stock
        verify(productRepository, times(1)).reserveStock(1L, 2);
        verify(eventPublisher, times(1)).publishInventoryReserved(501L, 1L, 2);

        // Second delivery (e.g. RabbitMQ redelivery after network reconnect): already processed
        when(processedEventRepository.existsById(eventId)).thenReturn(true);

        reservationService.handleOrderCreated(event);

        // Verify stock reservation was NOT executed again (idempotent guard)
        verify(productRepository, times(1)).reserveStock(1L, 2);
        verify(eventPublisher, times(1)).publishInventoryReserved(501L, 1L, 2);
    }

    @Test
    public void testDuplicateCancellationIdempotency() {
        String eventId = "cancel-dup-uuid-5678";
        OrderCancelledEvent event = new OrderCancelledEvent(eventId, 501L, 1L, 2, LocalDateTime.now());

        // First cancellation event
        when(processedEventRepository.existsById(eventId)).thenReturn(false);
        when(productRepository.releaseStock(1L, 2)).thenReturn(1);
        when(reservationRepository.findByOrderId(501L))
                .thenReturn(Optional.of(new InventoryReservation(501L, 1L, 2, ReservationStatus.RESERVED)));

        reservationService.handleOrderCancelled(event);

        verify(productRepository, times(1)).releaseStock(1L, 2);
        verify(eventPublisher, times(1)).publishStockReleased(501L, 1L, 2);

        // Second cancellation event (duplicate delivery)
        when(processedEventRepository.existsById(eventId)).thenReturn(true);

        reservationService.handleOrderCancelled(event);

        // Stock must NOT be restored a second time
        verify(productRepository, times(1)).releaseStock(1L, 2);
        verify(eventPublisher, times(1)).publishStockReleased(501L, 1L, 2);
    }
}
