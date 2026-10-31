package com.smartstock.orderservice;

import com.smartstock.orderservice.client.InventoryClient;
import com.smartstock.orderservice.client.ProductClientResponse;
import com.smartstock.orderservice.dto.CreateOrderRequest;
import com.smartstock.orderservice.dto.OrderResponse;
import com.smartstock.orderservice.event.InventoryRejectedEvent;
import com.smartstock.orderservice.event.InventoryReservedEvent;
import com.smartstock.orderservice.event.StockReleasedEvent;
import com.smartstock.orderservice.model.Order;
import com.smartstock.orderservice.model.OrderStatus;
import com.smartstock.orderservice.repository.OrderRepository;
import com.smartstock.orderservice.repository.ServiceEventLogRepository;
import com.smartstock.orderservice.service.OrderEventPublisher;
import com.smartstock.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class OrderWorkflowTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ServiceEventLogRepository eventLogRepository;

    @Mock
    private InventoryClient inventoryClient;

    @Mock
    private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateOrder_PublishesOrderCreated() {
        CreateOrderRequest request = new CreateOrderRequest("Bob", 1L, "Fresh Alphonso Mangoes", BigDecimal.valueOf(249.00), 2);
        ProductClientResponse product = new ProductClientResponse(1L, "Fresh Alphonso Mangoes", BigDecimal.valueOf(249.00), "img.jpg", 25, 25, 0);
        when(inventoryClient.getProductById(1L)).thenReturn(product);

        Order saved = new Order("Bob", 1L, "Fresh Alphonso Mangoes", 2, BigDecimal.valueOf(249.00), BigDecimal.valueOf(498.00), OrderStatus.PENDING);
        saved.setId(201L);
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(201L, response.getId());
        assertEquals(OrderStatus.PENDING, response.getStatus());

        verify(orderRepository).save(any(Order.class));
        verify(eventPublisher).publishOrderCreated(201L, 1L, 2);
    }

    @Test
    public void testHandleInventoryReserved_UpdatesStatusToPlaced() {
        Order order = new Order("Bob", 1L, "Fresh Alphonso Mangoes", 2, BigDecimal.valueOf(249.00), BigDecimal.valueOf(498.00), OrderStatus.PENDING);
        order.setId(201L);
        when(orderRepository.findById(201L)).thenReturn(Optional.of(order));

        InventoryReservedEvent event = new InventoryReservedEvent("evt-res-1", 201L, 1L, 2, LocalDateTime.now());
        orderService.handleInventoryReserved(event);

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.PLACED));
    }

    @Test
    public void testHandleInventoryRejected_UpdatesStatusToRejected() {
        Order order = new Order("Bob", 1L, "Fresh Alphonso Mangoes", 50, BigDecimal.valueOf(249.00), BigDecimal.valueOf(12450.00), OrderStatus.PENDING);
        order.setId(202L);
        when(orderRepository.findById(202L)).thenReturn(Optional.of(order));

        InventoryRejectedEvent event = new InventoryRejectedEvent("evt-rej-1", 202L, 1L, 50, 5, "Insufficient stock");
        orderService.handleInventoryRejected(event);

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.REJECTED));
    }

    @Test
    public void testCancelOrder_PublishesOrderCancelled() {
        Order order = new Order("Bob", 1L, "Fresh Alphonso Mangoes", 2, BigDecimal.valueOf(249.00), BigDecimal.valueOf(498.00), OrderStatus.PLACED);
        order.setId(201L);
        when(orderRepository.findById(201L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(201L);

        verify(eventPublisher).publishOrderCancelled(201L, 1L, 2);
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    public void testHandleStockReleased_ConfirmsCancelledStatus() {
        Order order = new Order("Bob", 1L, "Fresh Alphonso Mangoes", 2, BigDecimal.valueOf(249.00), BigDecimal.valueOf(498.00), OrderStatus.PLACED);
        order.setId(201L);
        when(orderRepository.findById(201L)).thenReturn(Optional.of(order));

        StockReleasedEvent event = new StockReleasedEvent("evt-rel-1", 201L, 1L, 2, LocalDateTime.now());
        orderService.handleStockReleased(event);

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    public void testGetCustomerOrders_OnlyReturnsPlacedAndCancelled() {
        Order placedOrder = new Order("Bob", 1L, "Mangoes", 2, BigDecimal.valueOf(249.00), BigDecimal.valueOf(498.00), OrderStatus.PLACED);
        placedOrder.setId(201L);
        Order cancelledOrder = new Order("Bob", 2L, "Paneer", 1, BigDecimal.valueOf(95.00), BigDecimal.valueOf(95.00), OrderStatus.CANCELLED);
        cancelledOrder.setId(202L);

        when(orderRepository.findByCustomerNameAndStatusInOrderByCreatedAtDesc(eq("Bob"), anyList()))
                .thenReturn(List.of(placedOrder, cancelledOrder));

        List<OrderResponse> customerOrders = orderService.getCustomerOrders("Bob");

        assertEquals(2, customerOrders.size());
        assertEquals(OrderStatus.PLACED, customerOrders.get(0).getStatus());
        assertEquals(OrderStatus.CANCELLED, customerOrders.get(1).getStatus());
    }
}
