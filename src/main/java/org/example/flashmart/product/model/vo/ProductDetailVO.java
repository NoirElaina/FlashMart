package org.example.flashmart.product.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain=true)
public class ProductDetailVO {
    private Integer id;
    private String name;
    private String category;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private Integer sold;
    private Boolean isHot;
    private Integer limitPerUser;
    private String mainImage;
    private List<String> images;
    private String description;
    private Map<String, String> specs;
    private SeckillVO seckill;
    private ReviewSummaryVO reviewSummary;
}
