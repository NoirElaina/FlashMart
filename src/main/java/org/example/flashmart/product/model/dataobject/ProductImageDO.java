package org.example.flashmart.product.model.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_images")
public class ProductImageDO {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer productId;
    private String url;
    private Integer sort;
    private Integer isMain;
    private LocalDateTime createTime;
}
