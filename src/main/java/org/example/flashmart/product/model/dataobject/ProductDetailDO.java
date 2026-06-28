package org.example.flashmart.product.model.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName("product_detail")
public class ProductDetailDO {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer productId;
    private String description;


    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> specs;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
