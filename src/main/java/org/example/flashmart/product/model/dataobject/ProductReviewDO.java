package org.example.flashmart.product.model.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("product_reviews")
public class ProductReviewDO {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer productId;
    private Integer userId;
    private Integer orderId;
    private Integer rating;
    private String content;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    private Integer isVisible;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
