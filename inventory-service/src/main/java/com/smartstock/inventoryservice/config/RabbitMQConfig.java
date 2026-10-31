package com.smartstock.inventoryservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "smartstock.exchange";
    public static final String DLX_NAME = "smartstock.dlx";
    public static final String DLQ_NAME = "smartstock.deadletter.queue";

    public static final String QUEUE_ORDER_CREATED = "order.created.queue";
    public static final String QUEUE_INVENTORY_RESERVED = "inventory.reserved.queue";
    public static final String QUEUE_INVENTORY_REJECTED = "inventory.rejected.queue";
    public static final String QUEUE_ORDER_CANCELLED = "order.cancelled.queue";
    public static final String QUEUE_INVENTORY_RELEASED = "inventory.released.queue";

    public static final String ROUTING_KEY_ORDER_CREATED = "order.created";
    public static final String ROUTING_KEY_ORDER_CANCELLED = "order.cancelled";
    public static final String ROUTING_KEY_INVENTORY_RESERVED = "inventory.reserved";
    public static final String ROUTING_KEY_INVENTORY_REJECTED = "inventory.rejected";
    public static final String ROUTING_KEY_INVENTORY_RELEASED = "inventory.released";

    @Bean
    public TopicExchange smartstockExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX_NAME, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ_NAME).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("#");
    }

    private Map<String, Object> queueArguments() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_NAME);
        args.put("x-dead-letter-routing-key", "deadletter");
        return args;
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_CREATED)
                .withArguments(queueArguments())
                .build();
    }

    @Bean
    public Queue inventoryReservedQueue() {
        return QueueBuilder.durable(QUEUE_INVENTORY_RESERVED)
                .withArguments(queueArguments())
                .build();
    }

    @Bean
    public Queue inventoryRejectedQueue() {
        return QueueBuilder.durable(QUEUE_INVENTORY_REJECTED)
                .withArguments(queueArguments())
                .build();
    }

    @Bean
    public Queue orderCancelledQueue() {
        return QueueBuilder.durable(QUEUE_ORDER_CANCELLED)
                .withArguments(queueArguments())
                .build();
    }

    @Bean
    public Queue inventoryReleasedQueue() {
        return QueueBuilder.durable(QUEUE_INVENTORY_RELEASED)
                .withArguments(queueArguments())
                .build();
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange smartstockExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(smartstockExchange).with(ROUTING_KEY_ORDER_CREATED);
    }

    @Bean
    public Binding orderCancelledBinding(Queue orderCancelledQueue, TopicExchange smartstockExchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(smartstockExchange).with(ROUTING_KEY_ORDER_CANCELLED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
