package org.example.flashmart.order.mq;


import lombok.extern.slf4j.Slf4j;
import org.example.flashmart.common.config.RabbitMqConfig;
import org.example.flashmart.order.event.OrderCreatedEvent;
import org.example.flashmart.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTimeoutConsumer {
    @Autowired
    private OrderService orderService;


    @RabbitListener(queues = RabbitMqConfig.ORDER_TIMEOUT_QUEUE)
    public void handleOrderTimeoutMessage(OrderCreatedEvent event) {
        // 消费者只触发超时检查；是否真的取消由订单服务按状态和 closeDeadlineTime 幂等判断。
        orderService.cancelExpiredOrder(event.getUserId(), event.getOrderId());
        log.info("订单超时检查完成，orderId={}", event.getOrderId());
    }

}
