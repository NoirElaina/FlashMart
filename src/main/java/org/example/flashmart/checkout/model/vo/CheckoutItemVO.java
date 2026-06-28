package org.example.flashmart.checkout.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckoutItemVO {
    private Integer cartId;
    private Integer productId;
    private String productName;
    private String productImage;
    private BigDecimal salePrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private Integer stock;
    private Boolean available;
    private String unavailableReason;
}
