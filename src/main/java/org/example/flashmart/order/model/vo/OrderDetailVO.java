package org.example.flashmart.order.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {
    private Long orderId;
    private String orderNo;
    private String status;
    private BigDecimal productAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    /**
     * 前端倒计时展示用的支付截止时间。
     */
    private LocalDateTime payExpireTime;
    /**
     * 后台清理截止时间，不应该作为用户可见的支付时间展示。
     */
    private LocalDateTime closeDeadlineTime;
    private LocalDateTime createTime;

    /**
     * 订单商品快照，展示历史订单时不能再依赖商品表当前价格和名称。
     */
    private List<OrderItemVO> items;
}
