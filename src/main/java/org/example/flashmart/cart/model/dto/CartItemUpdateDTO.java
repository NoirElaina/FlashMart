package org.example.flashmart.cart.model.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemUpdateDTO {
    @Min(value = 1, message = "商品数量至少为 1")
    private Integer quantity;

    @Min(value = 0, message = "勾选状态不合法")
    @Max(value = 1, message = "勾选状态不合法")
    private Integer selected;

    @AssertTrue(message = "至少提供一个更新字段")
    public boolean hasUpdateField() {
        return quantity != null || selected != null;
    }
}
