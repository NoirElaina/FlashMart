package org.example.flashmart.product.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewVO {
    private Integer id;
    private Integer rating;
    private String content;
    private List<String> images;
    private LocalDateTime createTime;
    private String nickname;
    private String avatar;
}
