package org.example.flashmart.cart.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemAddDTO {
    @NotNull(message = "商品 ID 不能为空")
    @Min(value = 1, message = "商品 ID 不合法")
    private Integer productId;

    @NotNull(message = "商品数量不能为空")
    @Min(value = 1, message = "商品数量至少为 1")
    private Integer quantity;
}
