package org.example.flashmart.order.model.dataobject;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String status;
    private BigDecimal productAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    /**
     * 用户可见的支付截止时间，支付接口以它作为能否付款的硬规则。
     */
    private LocalDateTime payExpireTime;
    /**
     * 后台自动关闭时间，通常晚于支付截止时间，只用于 MQ 超时清理。
     */
    private LocalDateTime closeDeadlineTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
