package org.example.flashmart.product.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductPriceUpdateDTO {
    @NotNull(message = "原价不能为空")
    @DecimalMin(value = "0.00", message = "原价不能为负")
    private BigDecimal originalPrice;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.00", message = "售价不能为负")
    private BigDecimal salePrice;
}
