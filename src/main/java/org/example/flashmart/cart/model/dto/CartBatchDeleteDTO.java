package org.example.flashmart.cart.model.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CartBatchDeleteDTO {
    @NotEmpty(message = "请选择要删除的购物车商品")
    private List<Integer> cartIds;
}
