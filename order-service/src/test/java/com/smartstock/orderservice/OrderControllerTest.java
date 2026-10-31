package com.smartstock.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstock.orderservice.controller.OrderController;
import com.smartstock.orderservice.dto.CancelOrderResponse;
import com.smartstock.orderservice.dto.CreateOrderRequest;
import com.smartstock.orderservice.dto.OrderResponse;
import com.smartstock.orderservice.exception.BusinessRuleConflictException;
import com.smartstock.orderservice.exception.GlobalExceptionHandler;
import com.smartstock.orderservice.exception.ResourceNotFoundException;
import com.smartstock.orderservice.model.Order;
import com.smartstock.orderservice.model.OrderStatus;
import com.smartstock.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class OrderControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void testCreateOrder() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest("Alice", 1L, "Fresh Alphonso Mangoes", BigDecimal.valueOf(249.00), 2);
        Order order = new Order("Alice", 1L, "Fresh Alphonso Mangoes", 2, BigDecimal.valueOf(249.00), BigDecimal.valueOf(498.00), OrderStatus.PENDING);
        order.setId(101L);

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(new OrderResponse(order));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.customerName").value("Alice"))
                .andExpect(jsonPath("$.totalAmount").value(498.00));
    }

    @Test
    public void testGetAllOrders() throws Exception {
        Order order = new Order("Alice", 1L, "Fresh Alphonso Mangoes", 2, BigDecimal.valueOf(249.00), BigDecimal.valueOf(498.00), OrderStatus.PLACED);
        order.setId(101L);

        when(orderService.getAllOrders()).thenReturn(List.of(new OrderResponse(order)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[0].status").value("PLACED"));
    }

    @Test
    public void testGetCustomerOrders() throws Exception {
        Order order = new Order("Alice", 1L, "Fresh Alphonso Mangoes", 2, BigDecimal.valueOf(249.00), BigDecimal.valueOf(498.00), OrderStatus.PLACED);
        order.setId(101L);

        when(orderService.getCustomerOrders("Alice")).thenReturn(List.of(new OrderResponse(order)));

        mockMvc.perform(get("/api/orders/customer/Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerName").value("Alice"))
                .andExpect(jsonPath("$[0].status").value("PLACED"));
    }

    @Test
    public void testCancelOrder_Success() throws Exception {
        when(orderService.cancelOrder(101L))
                .thenReturn(new CancelOrderResponse(101L, OrderStatus.CANCELLED, "Order #101 cancelled successfully."));

        mockMvc.perform(put("/api/orders/101/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    public void testCancelOrder_ConflictWhenAlreadyCancelled() throws Exception {
        when(orderService.cancelOrder(101L))
                .thenThrow(new BusinessRuleConflictException("Order #101 is already CANCELLED."));

        mockMvc.perform(put("/api/orders/101/cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Order #101 is already CANCELLED."));
    }
}
