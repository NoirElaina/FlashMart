package org.example.flashmart.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {
    private static final int USER_VISIBLE_PAYMENT_TIMEOUT_MILLIS = 15 * 60 * 1000;
    private static final int ORDER_TIMEOUT_GRACE_MILLIS = 60 * 1000;
    private static final int ORDER_TIMEOUT_DELAY_MILLIS = USER_VISIBLE_PAYMENT_TIMEOUT_MILLIS + ORDER_TIMEOUT_GRACE_MILLIS;

    public static final String ORDER_EXCHANGE = "flashmart.order.exchange";
    public static final String ORDER_CREATED_QUEUE = "flashmart.order.created.queue";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    // 这里的 16 分钟只用于后台清理触发；用户能否支付以 orders.pay_expire_time 和支付接口校验为准。
    public static final String ORDER_TIMEOUT_DELAY_QUEUE = "flashmart.order.timeout.delay.queue.v2";
    public static final String ORDER_TIMEOUT_QUEUE = "flashmart.order.timeout.queue";
    public static final String ORDER_TIMEOUT_DELAY_ROUTING_KEY = "order.timeout.delay.v2";
    public static final String ORDER_TIMEOUT_ROUTING_KEY = "order.timeout";


    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE, true);
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue()).to(orderExchange()).with(ORDER_CREATED_ROUTING_KEY);
    }


    @Bean
    public Queue orderTimeoutDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", ORDER_TIMEOUT_DELAY_MILLIS);
        args.put("x-dead-letter-exchange", ORDER_EXCHANGE);
        args.put("x-dead-letter-routing-key", ORDER_TIMEOUT_ROUTING_KEY);
        return new Queue(ORDER_TIMEOUT_DELAY_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return new Queue(ORDER_TIMEOUT_QUEUE, true);
    }

    @Bean
    public Binding orderTimeoutDelayBinding() {
        return BindingBuilder.bind(orderTimeoutDelayQueue()).to(orderExchange()).with(ORDER_TIMEOUT_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding orderTimeoutBinding() {
        return BindingBuilder.bind(orderTimeoutQueue()).to(orderExchange()).with(ORDER_TIMEOUT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
