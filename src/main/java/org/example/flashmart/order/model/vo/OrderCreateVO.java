package org.example.flashmart.order.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCreateVO {
    private Long orderId;
    private String orderNo;
    private String status;
    private BigDecimal payableAmount;
}