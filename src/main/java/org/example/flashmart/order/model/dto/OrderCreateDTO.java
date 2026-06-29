package org.example.flashmart.order.model.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {
    /**
     * CART: 从购物车选中项创建订单。
     * BUY_NOW: 从商品详情页立即购买创建订单。
     */
    private String mode;

    private List<Integer> cartIds;

    private Integer productId;

    private Integer quantity;

    /**
     * 下单幂等令牌，由 GET /api/orders/token 获取，提交订单时回传。
     */
    private String idempotencyToken;

    @NotBlank(message = "收货人不能为空")
    @Size(max = 64, message = "收货人不能超过64个字符")
    private String receiverName;

    @NotBlank(message = "手机号不能为空")
    @Size(max = 32, message = "手机号不能超过32个字符")
    private String receiverPhone;

    @NotBlank(message = "收货地址不能为空")
    @Size(max = 255, message = "收货地址不能超过255个字符")
    private String receiverAddress;
}
