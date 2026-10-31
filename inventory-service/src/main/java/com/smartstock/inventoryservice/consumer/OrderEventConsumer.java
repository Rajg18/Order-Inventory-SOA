package com.smartstock.inventoryservice.consumer;

import com.smartstock.inventoryservice.config.RabbitMQConfig;
import com.smartstock.inventoryservice.event.OrderCancelledEvent;
import com.smartstock.inventoryservice.event.OrderCreatedEvent;
import com.smartstock.inventoryservice.service.InventoryReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final InventoryReservationService reservationService;

    public OrderEventConsumer(InventoryReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_CREATED)
    public void consumeOrderCreated(OrderCreatedEvent event) {
        log.info("Consumer received OrderCreatedEvent for orderId: {}", event.getOrderId());
        reservationService.handleOrderCreated(event);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ORDER_CANCELLED)
    public void consumeOrderCancelled(OrderCancelledEvent event) {
        log.info("Consumer received OrderCancelledEvent for orderId: {}", event.getOrderId());
        reservationService.handleOrderCancelled(event);
    }
}
