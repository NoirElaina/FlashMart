package org.example.flashmart.order.controller;


import jakarta.validation.Valid;
import org.example.flashmart.common.response.Result;
import org.example.flashmart.order.model.dto.OrderCreateDTO;
import org.example.flashmart.order.model.vo.OrderCreateVO;
import org.example.flashmart.order.model.vo.OrderDetailVO;
import org.example.flashmart.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/token")
    public Result<String> issueOrderToken() {
        return Result.success(orderService.issueOrderToken());
    }

    @PostMapping
    public Result<OrderCreateVO> createOrder(@RequestAttribute Long userId, @Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.createOrder(userId, dto));
    }

    @PostMapping("/seckill")
    public Result<OrderCreateVO> createSeckillOrder(@RequestAttribute Long userId, @Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.createSeckillOrder(userId, dto));
    }

    @GetMapping
    public Result<List<OrderDetailVO>> listOrders(@RequestAttribute Long userId) {
        return Result.success(orderService.listOrders(userId));
    }

    @GetMapping("/{orderId}")
    public Result<OrderDetailVO> getOrderDetail(@RequestAttribute Long userId, @PathVariable("orderId") Long orderId) {
        return Result.success(orderService.getOrderDetail(userId, orderId));
    }

    @PutMapping("/{orderId}/cancel")
    public Result<String> cancelOrder(@RequestAttribute Long userId, @PathVariable Long orderId) {
        orderService.cancelOrder(userId, orderId);
        return Result.success("取消订单成功");
    }
    @PutMapping("/{orderId}/pay")
    public Result<String> payOrder(@RequestAttribute Long userId,
                                   @PathVariable Long orderId) {
        orderService.payOrder(userId, orderId);
        return Result.success("支付成功");
    }
}
