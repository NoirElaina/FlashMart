package org.example.flashmart.product.model.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ProductQuery {
    @Min(value = 1, message = "页码至少为 1")
    private Integer page = 1;

    @Min(value = 1, message = "每页数量至少为 1")
    @Max(value = 100, message = "每页数量不能超过 100")
    private Integer pageSize = 10;

    private String category;
}
