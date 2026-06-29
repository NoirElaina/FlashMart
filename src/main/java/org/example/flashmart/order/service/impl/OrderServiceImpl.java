package org.example.flashmart.order.service.impl;

import org.example.flashmart.cart.mapper.CartMapper;
import org.example.flashmart.cart.model.queryobject.CartItemQueryObject;
import org.example.flashmart.common.config.RabbitMqConfig;
import org.example.flashmart.common.exception.BusinessException;
import org.example.flashmart.order.event.OrderCreatedEvent;
import org.example.flashmart.order.mapper.OrderMapper;
import org.example.flashmart.order.model.dataobject.OrderDO;
import org.example.flashmart.order.model.dataobject.OrderItemDO;
import org.example.flashmart.order.model.dto.OrderCreateDTO;
import org.example.flashmart.order.model.vo.OrderCreateVO;
import org.example.flashmart.order.model.vo.OrderDetailVO;
import org.example.flashmart.order.model.vo.OrderItemVO;
import org.example.flashmart.order.service.OrderService;
import org.example.flashmart.order.service.OrderTokenService;
import org.example.flashmart.product.mapper.ProductMapper;
import org.example.flashmart.product.model.dataobject.ProductDO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class OrderServiceImpl implements OrderService {
    private static final String ORDER_STATUS_PENDING_PAYMENT = "PENDING_PAYMENT";
    private static final Duration PAYMENT_TIMEOUT = Duration.ofMinutes(15);
    private static final Duration AUTO_CANCEL_GRACE = Duration.ofMinutes(1);

    @Autowired
    private  CartMapper cartMapper;
    @Autowired
    private  ProductMapper productMapper;
    @Autowired
    private  OrderMapper orderMapper;
    @Autowired
    private RabbitTemplate  rabbitTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private OrderTokenService orderTokenService;

    @Override
    public String issueOrderToken() {
        return orderTokenService.issue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateVO createOrder(Long userId, OrderCreateDTO dto) {
        // 幂等校验：消费一次性 token，重复提交的第二次拿不到 token 会被直接拦下。
        if (!orderTokenService.consume(dto.getIdempotencyToken())) {
            throw new BusinessException("请勿重复提交订单");
        }
        return createOrderInternal(userId, dto, false);
    }

    private OrderCreateVO createOrderInternal(Long userId, OrderCreateDTO dto, boolean allowSeckillProduct) {
        // 解析mode，看看是直接购买还是购物车购买
        String mode = dto.getMode();
        if (!"CART".equals(mode) && !"BUY_NOW".equals(mode)) {
            throw new BusinessException("下单来源不合法");
        }

        List<OrderItemDO> orderItems;

        // 先把购物车和立即购买统一成订单项快照，后续金额、落库、扣库存走同一套流程。
        if (mode.equals("CART")) {
            if (dto.getCartIds() == null || dto.getCartIds().isEmpty()) {
                throw new BusinessException("请选择要下单的商品");
            }

            List<CartItemQueryObject> cartItems = cartMapper.selectCartItemsByIds(userId, dto.getCartIds());
            if (cartItems.isEmpty()) {
                throw new BusinessException(404, "下单商品不存在");
            }
            if (cartItems.size() != dto.getCartIds().size()) {
                throw new BusinessException(404, "部分下单商品不存在");
            }
            if (!allowSeckillProduct && cartItems.stream().anyMatch(item -> Boolean.TRUE.equals(item.getSeckill()))) {
                throw new BusinessException("秒杀商品请通过秒杀入口下单");
            }
            orderItems = cartItems.stream().map(this::toOrderItemFromCart)
                    .toList();
        } else {
            // 立即购买不经过购物车，直接从商品表读取最新价格、状态和库存。
            if (dto.getProductId() == null || dto.getProductId() <= 0) {
                throw new BusinessException("请选择要购买的商品");
            }
            if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
                throw new BusinessException("购买数量不合法");
            }

            ProductDO product = productMapper.selectById(Long.valueOf(dto.getProductId()));
            if (product == null) {
                throw new BusinessException(404, "商品不存在");
            }
            if (!Integer.valueOf(1).equals(product.getStatus())) {
                throw new BusinessException("商品已下架");
            }
            if (!allowSeckillProduct && Boolean.TRUE.equals(product.getSeckill())) {
                throw new BusinessException("秒杀商品请通过秒杀入口下单");
            }
            if (product.getStock() == null || product.getStock() < dto.getQuantity()) {
                throw new BusinessException("商品库存不足");
            }
            if (product.getLimitPerUser() != null && dto.getQuantity() > product.getLimitPerUser()) {
                throw new BusinessException("超过商品限购数量");
            }
            orderItems = List.of(toOrderItemFromProduct(product, dto.getQuantity()));
        }


        // 当前阶段只计算商品金额，运费和优惠先预留为 0，后续接优惠券/运费模板时再替换这里。
        BigDecimal productAmount = orderItems.stream().map(OrderItemDO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal payableAmount = productAmount.add(shippingFee).subtract(discountAmount);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime payExpireTime = now.plus(PAYMENT_TIMEOUT);
        LocalDateTime closeDeadlineTime = payExpireTime.plus(AUTO_CANCEL_GRACE);

        // 支付截止时间是业务硬规则；后台关闭时间只给 MQ 清理留缓冲，不代表用户还能继续支付。
        OrderDO order = new OrderDO();
        order.setOrderNo(generateOrderNo(userId));
        order.setUserId(userId);
        order.setStatus(ORDER_STATUS_PENDING_PAYMENT);
        order.setProductAmount(productAmount);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setPayableAmount(payableAmount);
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setPayExpireTime(payExpireTime);
        order.setCloseDeadlineTime(closeDeadlineTime);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        orderMapper.insertOrder(order);

        // insertOrder 会回填自增 order.id，订单明细必须使用这个真实主键关联订单。
        for (OrderItemDO item : orderItems) {
            item.setOrderId(order.getId());
        }
        orderMapper.insertOrderItems(orderItems);

        // 扣库存是下单链路的核心校验；任一商品扣减失败，事务会回滚订单和明细。
        for (OrderItemDO item : orderItems) {
            int affected = orderMapper.deductStock(item.getProductId().intValue(), item.getQuantity());
            if (affected <= 0) {
                throw new BusinessException("商品库存不足");
            }
        }

        // 购物车下单成功后清理本次勾选商品；立即购买不应该影响购物车。
        if ("CART".equals(mode)) {
            int deleted = cartMapper.deleteBatchByUserId(userId, dto.getCartIds());
            if (deleted != dto.getCartIds().size()) {
                throw new BusinessException("部分购物车商品删除失败");
            }
        }

        // 事务提交后发送订单创建事件，避免订单回滚但 MQ 消息已经发出。
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                order.getId(),
                order.getOrderNo(),
                userId,
                now
        );
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(
                        RabbitMqConfig.ORDER_EXCHANGE,
                        RabbitMqConfig.ORDER_CREATED_ROUTING_KEY,
                        event
                );
                rabbitTemplate.convertAndSend(
                        RabbitMqConfig.ORDER_EXCHANGE,
                        RabbitMqConfig.ORDER_TIMEOUT_DELAY_ROUTING_KEY,
                        event
                );
            }

        });

        // 前端只需要订单 id、订单号、状态和应付金额来跳转详情页或展示提交结果。
        OrderCreateVO vo = new OrderCreateVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setPayableAmount(order.getPayableAmount());
        return vo;
    }

    @Override
    public List<OrderDetailVO> listOrders(Long userId) {
        List<OrderDetailVO> orders = orderMapper.listOrders(userId);
        if (orders.isEmpty()) {
            return orders;
        }

        List<Long> orderIds = orders.stream()
                .map(OrderDetailVO::getOrderId)
                .toList();

        Map<Long, List<OrderItemVO>> itemMap = orderMapper.selectOrderItemsByOrderIds(orderIds)
                .stream()
                .collect(Collectors.groupingBy(OrderItemVO::getOrderId));

        for (OrderDetailVO order : orders) {
            order.setItems(itemMap.getOrDefault(order.getOrderId(), List.of()));
        }
        return orders;
    }

    @Override
    public OrderDetailVO getOrderDetail(Long userId, Long orderId) {
        OrderDetailVO order = orderMapper.selectOrderDetail(userId, orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        order.setItems(orderMapper.selectOrderItems(orderId));
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, Long orderId) {
        OrderDetailVO order = orderMapper.selectOrderDetail(userId, orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!ORDER_STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new BusinessException("只能取消待支付订单");
        }
        List<OrderItemVO> items = orderMapper.selectOrderItems(orderId);
        if (items.isEmpty()) {
            throw new BusinessException("订单明细不存在");
        }
        int affected = orderMapper.cancelOrder(userId, orderId);
        if (affected == 0) {
            throw new BusinessException("订单不存在或当前状态不可取消");
        }
        restoreStock(items);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long userId, Long orderId) {
        OrderDetailVO order = orderMapper.selectOrderDetail(userId, orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        if (!ORDER_STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            throw new BusinessException("订单不存在或当前状态不可支付");
        }

        // 支付接口必须主动校验业务截止时间，不能只依赖 MQ 是否已经完成超时取消。
        if (LocalDateTime.now().isAfter(order.getPayExpireTime())) {
            cancelExpiredOrder(userId, orderId);
            throw new BusinessException("订单已超时，请重新下单");
        }
        int affected = orderMapper.payOrder(userId, orderId);
        if (affected == 0) {
            cancelExpiredOrder(userId, orderId);
            throw new BusinessException("订单已超时或当前状态不可支付");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelExpiredOrder(Long userId, Long orderId) {
        OrderDetailVO order = orderMapper.selectOrderDetail(userId, orderId);
        if (order == null || !ORDER_STATUS_PENDING_PAYMENT.equals(order.getStatus())) {
            return;
        }

        // MQ 的延迟时间只是触发清理的时间；如果消息提前到达，不能误取消还在支付期内的订单。
        if (LocalDateTime.now().isBefore(order.getCloseDeadlineTime())) {
            return;
        }

        List<OrderItemVO> items = orderMapper.selectOrderItems(orderId);
        if (items.isEmpty()) {
            throw new BusinessException("订单明细不存在");
        }

        int affected = orderMapper.cancelOrder(userId, orderId);
        if (affected == 0) {
            return;
        }
        restoreStock(items);
    }

    @Override
    public OrderCreateVO createSeckillOrder(Long userId, OrderCreateDTO dto) {
        if (dto.getProductId()==null||dto.getProductId()<=0) {
            throw new BusinessException("请选择要秒杀的商品");
        }
        String lockKey = "flashmart:lock:seckill:user:" + dto.getProductId() + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked;

        try {
            locked=lock.tryLock(0, TimeUnit.SECONDS);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new BusinessException("秒杀请求被中断，请稍后重试");
        }
        if (!locked) {
            throw new BusinessException("秒杀请求过于频繁，请稍后重试");
        }
        try {
            OrderCreateVO result = transactionTemplate.execute(status -> doCreateSeckillOrder(userId, dto));
            if (result == null) {
                throw new BusinessException("秒杀下单失败，请稍后重试");
            }
            return result;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private OrderCreateVO doCreateSeckillOrder(Long userId, OrderCreateDTO dto) {
        ProductDO product = productMapper.selectById(Long.valueOf(dto.getProductId()));
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!Boolean.TRUE.equals(product.getSeckill())) {
            throw new BusinessException("当前商品不是秒杀商品");
        }
        if (!Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException("商品已下架");
        }

        LocalDateTime now = LocalDateTime.now();
        if (product.getSeckillStartTime() == null || now.isBefore(product.getSeckillStartTime())) {
            throw new BusinessException("秒杀尚未开始");
        }
        if (product.getSeckillEndTime() == null || now.isAfter(product.getSeckillEndTime())) {
            throw new BusinessException("秒杀已结束");
        }

        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BusinessException("购买数量不合法");
        }
        if (product.getLimitPerUser() != null && dto.getQuantity() > product.getLimitPerUser()) {
            throw new BusinessException("超过商品限购数量");
        }

        dto.setMode("BUY_NOW");
        return createOrderInternal(userId, dto, true);
    }

    private String generateOrderNo(Long userId) {
        return "FM" + System.currentTimeMillis() + userId;
    }

    private void restoreStock(List<OrderItemVO> items) {
        for (OrderItemVO item : items) {
            int restored = orderMapper.restoreStock(item.getProductId().intValue(), item.getQuantity());
            if (restored == 0) {
                throw new BusinessException("订单取消失败，库存回滚异常");
            }
        }
    }

    private OrderItemDO toOrderItemFromCart(CartItemQueryObject item) {
        // 购物车中的库存可能已经变化，生成订单项前仍要做一次库存校验。
        if (item.getStock() == null || item.getStock() < item.getQuantity()) {
            throw new BusinessException("商品库存不足");
        }

        OrderItemDO orderItem = new OrderItemDO();
        orderItem.setProductId(Long.valueOf(item.getProductId()));
        orderItem.setProductName(item.getName());
        orderItem.setProductImage(item.getImage());
        orderItem.setProductPrice(item.getSalePrice());
        orderItem.setQuantity(item.getQuantity());
        orderItem.setSubtotal(item.getSalePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        return orderItem;
    }

    private OrderItemDO toOrderItemFromProduct(ProductDO product, Integer quantity) {
        // 立即购买使用商品当前信息生成快照，避免后续商品改价影响历史订单。
        OrderItemDO orderItem = new OrderItemDO();
        orderItem.setProductId(Long.valueOf(product.getId()));
        orderItem.setProductName(product.getName());
        orderItem.setProductImage(product.getImage());
        orderItem.setProductPrice(product.getSalePrice());
        orderItem.setQuantity(quantity);
        orderItem.setSubtotal(product.getSalePrice().multiply(BigDecimal.valueOf(quantity)));
        return orderItem;
    }
}
