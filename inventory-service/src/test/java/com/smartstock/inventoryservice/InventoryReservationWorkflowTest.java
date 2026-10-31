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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class InventoryReservationWorkflowTest {

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
    public void testOrderCreated_StockSufficient_ReservesAndPublishes() {
        OrderCreatedEvent event = new OrderCreatedEvent("evt-1", 101L, 1L, 2, LocalDateTime.now());

        when(processedEventRepository.existsById("evt-1")).thenReturn(false);
        when(productRepository.reserveStock(1L, 2)).thenReturn(1); // 1 row updated

        reservationService.handleOrderCreated(event);

        verify(productRepository).reserveStock(1L, 2);
        verify(reservationRepository).save(any(InventoryReservation.class));
        verify(eventPublisher).publishInventoryReserved(101L, 1L, 2);
        verify(eventPublisher, never()).publishInventoryRejected(any(), any(), any(), any(), any());
    }

    @Test
    public void testOrderCreated_StockInsufficient_RejectsAndPublishes() {
        OrderCreatedEvent event = new OrderCreatedEvent("evt-2", 102L, 1L, 50, LocalDateTime.now());

        when(processedEventRepository.existsById("evt-2")).thenReturn(false);
        when(productRepository.reserveStock(1L, 50)).thenReturn(0); // 0 rows updated
        Product product = new Product(1L, "Mangoes", BigDecimal.valueOf(249.00), "img.jpg", 25, 10, 15);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        reservationService.handleOrderCreated(event);

        verify(productRepository).reserveStock(1L, 50);
        verify(reservationRepository, never()).save(any(InventoryReservation.class));
        verify(eventPublisher).publishInventoryRejected(eq(102L), eq(1L), eq(50), eq(10), anyString());
        verify(eventPublisher, never()).publishInventoryReserved(any(), any(), any());
    }

    @Test
    public void testOrderCreated_DuplicateEvent_IdempotentlyDiscarded() {
        OrderCreatedEvent event = new OrderCreatedEvent("evt-dup", 103L, 1L, 2, LocalDateTime.now());

        when(processedEventRepository.existsById("evt-dup")).thenReturn(true);

        reservationService.handleOrderCreated(event);

        verify(productRepository, never()).reserveStock(any(), any());
        verify(eventPublisher, never()).publishInventoryReserved(any(), any(), any());
        verify(eventPublisher, never()).publishInventoryRejected(any(), any(), any(), any(), any());
    }

    @Test
    public void testOrderCancelled_StockReleasedAndStatusUpdated() {
        OrderCancelledEvent event = new OrderCancelledEvent("evt-cancel-1", 101L, 1L, 2, LocalDateTime.now());

        when(processedEventRepository.existsById("evt-cancel-1")).thenReturn(false);
        when(productRepository.releaseStock(1L, 2)).thenReturn(1);
        InventoryReservation res = new InventoryReservation(101L, 1L, 2, ReservationStatus.RESERVED);
        when(reservationRepository.findByOrderId(101L)).thenReturn(Optional.of(res));

        reservationService.handleOrderCancelled(event);

        verify(productRepository).releaseStock(1L, 2);
        verify(reservationRepository).save(argThat(r -> r.getReservationStatus() == ReservationStatus.RELEASED));
        verify(eventPublisher).publishStockReleased(101L, 1L, 2);
    }
}
