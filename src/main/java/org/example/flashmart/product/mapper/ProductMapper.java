package org.example.flashmart.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.flashmart.product.model.dataobject.ProductDO;

import java.util.List;

@Mapper
public interface ProductMapper {

    /**
     * 首页商品分页查询。category 为空时返回全部分类。
     */
    List<ProductDO> pageProducts(@Param("offset") Integer offset,
                                 @Param("pageSize") Integer pageSize,
                                 @Param("category") String category);

    /**
     * 和 pageProducts 使用同一套筛选条件，保证分页总数一致。
     */
    long countProducts(@Param("category") String category);

    /**
     * 商品详情、购物车校验、立即购买都会复用这个查询。
     */
    @Select("SELECT id, name, image, category, original_price as originalPrice, sale_price as  salePrice, stock, sold, is_hot as hot, is_seckill as seckill, limit_per_user as limitPerUser, seckill_start_time as seckillStartTime, seckill_end_time as seckillEndTime, status, create_time as createTime, update_time as updateTime  FROM products WHERE id = #{productId}")
    ProductDO selectById(Long productId);

    @Update("""
        update products
        set stock = stock + #{quantity},
            sold = sold - #{quantity},
            update_time = now()
        where id = #{productId}
          and sold >= #{quantity}
        """)
    int restoreStock(@Param("productId") Integer productId,
                     @Param("quantity") Integer quantity);
}
