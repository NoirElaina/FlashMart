package org.example.flashmart.checkout.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckoutPreviewDTO {
    /**
     * CART: 从购物车选中项结算。
     * BUY_NOW: 商品详情页立即购买直接结算。
     * SECKILL: 秒杀商品直接结算，提交订单时会走秒杀下单接口。
     */
    private String mode;

    private List<Integer> cartIds;

    private Integer productId;

    private Integer quantity;
}
