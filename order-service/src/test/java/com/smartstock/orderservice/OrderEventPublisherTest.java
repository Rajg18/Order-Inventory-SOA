package com.smartstock.orderservice;

import com.smartstock.orderservice.config.RabbitMQConfig;
import com.smartstock.orderservice.event.OrderCancelledEvent;
import com.smartstock.orderservice.event.OrderCreatedEvent;
import com.smartstock.orderservice.model.ServiceEventLog;
import com.smartstock.orderservice.repository.ServiceEventLogRepository;
import com.smartstock.orderservice.service.OrderEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class OrderEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ServiceEventLogRepository eventLogRepository;

    @InjectMocks
    private OrderEventPublisher publisher;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPublishOrderCreated() {
        String eventId = publisher.publishOrderCreated(101L, 1L, 2);

        assertNotNull(eventId);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY_ORDER_CREATED),
                any(OrderCreatedEvent.class)
        );

        ArgumentCaptor<ServiceEventLog> captor = ArgumentCaptor.forClass(ServiceEventLog.class);
        verify(eventLogRepository).save(captor.capture());
        assertEquals("OrderCreated", captor.getValue().getEventType());
        assertEquals("PUBLISHED", captor.getValue().getDirection());
        assertEquals(101L, captor.getValue().getOrderId());
    }

    @Test
    public void testPublishOrderCancelled() {
        String eventId = publisher.publishOrderCancelled(101L, 1L, 2);

        assertNotNull(eventId);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY_ORDER_CANCELLED),
                any(OrderCancelledEvent.class)
        );

        ArgumentCaptor<ServiceEventLog> captor = ArgumentCaptor.forClass(ServiceEventLog.class);
        verify(eventLogRepository).save(captor.capture());
        assertEquals("OrderCancelled", captor.getValue().getEventType());
        assertEquals("PUBLISHED", captor.getValue().getDirection());
        assertEquals(101L, captor.getValue().getOrderId());
    }
}
