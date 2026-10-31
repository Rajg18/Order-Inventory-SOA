package com.smartstock.inventoryservice;

import com.smartstock.inventoryservice.config.RabbitMQConfig;
import com.smartstock.inventoryservice.event.InventoryRejectedEvent;
import com.smartstock.inventoryservice.event.InventoryReservedEvent;
import com.smartstock.inventoryservice.event.StockReleasedEvent;
import com.smartstock.inventoryservice.model.ServiceEventLog;
import com.smartstock.inventoryservice.repository.ServiceEventLogRepository;
import com.smartstock.inventoryservice.service.InventoryEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class InventoryEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ServiceEventLogRepository eventLogRepository;

    @InjectMocks
    private InventoryEventPublisher publisher;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPublishInventoryReserved() {
        String eventId = publisher.publishInventoryReserved(101L, 1L, 2);

        assertNotNull(eventId);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY_INVENTORY_RESERVED),
                any(InventoryReservedEvent.class)
        );

        ArgumentCaptor<ServiceEventLog> captor = ArgumentCaptor.forClass(ServiceEventLog.class);
        verify(eventLogRepository).save(captor.capture());
        assertEquals("InventoryReserved", captor.getValue().getEventType());
        assertEquals("PUBLISHED", captor.getValue().getDirection());
        assertEquals(101L, captor.getValue().getOrderId());
    }

    @Test
    public void testPublishInventoryRejected() {
        String eventId = publisher.publishInventoryRejected(101L, 1L, 50, 5, "Insufficient stock");

        assertNotNull(eventId);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY_INVENTORY_REJECTED),
                any(InventoryRejectedEvent.class)
        );

        ArgumentCaptor<ServiceEventLog> captor = ArgumentCaptor.forClass(ServiceEventLog.class);
        verify(eventLogRepository).save(captor.capture());
        assertEquals("InventoryRejected", captor.getValue().getEventType());
        assertEquals("PUBLISHED", captor.getValue().getDirection());
        assertEquals(101L, captor.getValue().getOrderId());
    }

    @Test
    public void testPublishStockReleased() {
        String eventId = publisher.publishStockReleased(101L, 1L, 2);

        assertNotNull(eventId);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY_INVENTORY_RELEASED),
                any(StockReleasedEvent.class)
        );

        ArgumentCaptor<ServiceEventLog> captor = ArgumentCaptor.forClass(ServiceEventLog.class);
        verify(eventLogRepository).save(captor.capture());
        assertEquals("StockReleased", captor.getValue().getEventType());
        assertEquals("PUBLISHED", captor.getValue().getDirection());
        assertEquals(101L, captor.getValue().getOrderId());
    }
}
