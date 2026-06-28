package org.example.flashmart.cart.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartVO {
    private Integer id;
    private Integer productId;
    private String name;
    private String image;
    private String category;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer quantity;
    private Integer selected;
    private Integer stock;
    private Integer sold;
    private Integer limitPerUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
