package org.example.flashmart.cart.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CartSelectedBatchUpdateDTO {
    @NotEmpty(message = "请选择购物车商品")
    private List<Integer> cartIds;

    @NotNull(message = "请选择勾选状态")
    private Integer selected;
}
