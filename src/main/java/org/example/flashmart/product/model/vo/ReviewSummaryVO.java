package org.example.flashmart.product.model.vo;


import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ReviewSummaryVO {
    private Double avgRating;
    private Integer totalCount;
    private Map<Integer, Integer> distribution;
    private List<ReviewVO> latestReviews;
}
