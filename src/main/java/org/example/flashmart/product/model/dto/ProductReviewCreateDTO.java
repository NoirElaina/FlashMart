package org.example.flashmart.product.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProductReviewCreateDTO {
    @NotNull(message = "商品ID不能为空")
    private Integer productId;

    private Integer orderId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    private Integer rating;

    @Size(max = 500, message = "评论内容不超过500字")
    private String content;

    private List<String> images;
}
