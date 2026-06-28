package org.example.flashmart.order.service;

import jakarta.validation.Valid;
import org.example.flashmart.order.model.dto.OrderCreateDTO;
import org.example.flashmart.order.model.vo.OrderCreateVO;
import org.example.flashmart.order.model.vo.OrderDetailVO;

import java.util.List;

public interface OrderService {
    OrderCreateVO createOrder(Long userId, OrderCreateDTO dto);

    List<OrderDetailVO> listOrders(Long userId);

    OrderDetailVO getOrderDetail(Long userId, Long orderId);

    void cancelOrder(Long userId, Long orderId);

    void payOrder(Long userId, Long orderId);

    /**
     * MQ 超时检查使用的幂等取消入口：只清理真正过期且仍待支付的订单。
     */
    void cancelExpiredOrder(Long userId, Long orderId);

    OrderCreateVO createSeckillOrder(Long userId, @Valid OrderCreateDTO dto);
}
