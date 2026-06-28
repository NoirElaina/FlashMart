package org.example.flashmart.product.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeckillVO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long countdownSeconds;
    private Boolean isActive;
    private String statusText;
    private StockVO stock;
}
