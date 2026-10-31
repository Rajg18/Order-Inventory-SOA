package com.smartstock.orderservice.consumer;

import com.smartstock.orderservice.config.RabbitMQConfig;
import com.smartstock.orderservice.event.InventoryRejectedEvent;
import com.smartstock.orderservice.event.InventoryReservedEvent;
import com.smartstock.orderservice.event.StockReleasedEvent;
import com.smartstock.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final OrderService orderService;

    public InventoryEventConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_INVENTORY_RESERVED)
    public void consumeInventoryReserved(InventoryReservedEvent event) {
        log.info("Consumer received InventoryReservedEvent for Order #{}", event.getOrderId());
        orderService.handleInventoryReserved(event);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_INVENTORY_REJECTED)
    public void consumeInventoryRejected(InventoryRejectedEvent event) {
        log.info("Consumer received InventoryRejectedEvent for Order #{}: {}", event.getOrderId(), event.getReason());
        orderService.handleInventoryRejected(event);
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_INVENTORY_RELEASED)
    public void consumeStockReleased(StockReleasedEvent event) {
        log.info("Consumer received StockReleasedEvent for Order #{}", event.getOrderId());
        orderService.handleStockReleased(event);
    }
}
