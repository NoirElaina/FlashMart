package org.example.flashmart.cart.model.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("shopping_cart")
public class ShoppingCartDO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private Integer selected;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
