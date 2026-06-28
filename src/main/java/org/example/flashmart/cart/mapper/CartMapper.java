package org.example.flashmart.cart.mapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.apache.ibatis.annotations.*;
import org.example.flashmart.cart.model.dataobject.ShoppingCartDO;
import org.example.flashmart.cart.model.dto.CartItemUpdateDTO;
import org.example.flashmart.cart.model.queryobject.CartItemQueryObject;

import java.util.List;

@Mapper
public interface CartMapper {

    /**
     * 查询当前用户购物车展示数据，顺带查商品快照信息用于前端展示。
     */
    @Select("""
            select
                c.id,
                c.product_id as productId,
                p.name,
                p.image,
                p.category,
                p.original_price as originalPrice,
                p.sale_price as salePrice,
                c.quantity,
                c.selected,
                p.stock,
                p.sold,
                p.is_seckill as seckill,
                p.limit_per_user as limitPerUser,
                c.create_time as createTime,
                c.update_time as updateTime
            from shopping_cart c
            left join products p on c.product_id = p.id
            where c.user_id = #{userId}
            order by c.update_time desc
            """)
    List<CartItemQueryObject> selectCartItemsByUserId(@Param("userId") Long userId);

    /**
     * 加购物车前查重：同一用户同一商品只保留一条购物车记录，后续走数量合并。
     */
    @Select("""
            select
                id,
                user_id as userId,
                product_id as productId,
                quantity,
                selected,
                create_time as createTime,
                update_time as updateTime
            from shopping_cart
            where user_id = #{userId} and product_id = #{productId}
            limit 1
            """)
    ShoppingCartDO selectByUserIdAndProductId(@Param("userId") Long userId,
                                              @Param("productId") Integer productId);

    /**
     * 按购物车 ID 和用户 ID 一起查询，避免用户操作到别人的购物车项。
     */
    @Select("""
            select
                id,
                user_id as userId,
                product_id as productId,
                quantity,
                selected,
                create_time as createTime,
                update_time as updateTime
            from shopping_cart
            where id = #{cartId} and user_id = #{userId}
            limit 1
            """)
    ShoppingCartDO selectByIdAndUserId(@Param("cartId") Integer cartId,
                                       @Param("userId") Long userId);

    /**
     * 新增购物车项。库存和限购校验放在 Service 层，Mapper 只负责落库。
     */
    @Insert("""
            insert into shopping_cart (user_id, product_id, quantity, selected, create_time, update_time)
            values (#{userId}, #{productId}, #{quantity}, #{selected}, now(), now())
            """)
    void insert(ShoppingCartDO shoppingCartDO);

    @Update("""
            update shopping_cart
            set quantity = #{quantity},
                update_time = now()
            where id = #{cartId}
            """)
    void updateQuantityById(@Param("cartId") Integer cartId,
                            @Param("quantity") Integer quantity);

    /**
     * 更新数量或勾选状态，where 中必须带 userId 做用户隔离。
     */
    void updateCart(@Param("userId") Long userId,
                    @Param("cartId") @Positive Integer cartId,
                    @Param("cartItemUpdateDTO") @Valid CartItemUpdateDTO cartItemUpdateDTO);

    /**
     * 删除当前用户自己的购物车项，返回影响行数给 Service 判断是否真的删除成功。
     */
    @Delete("""
            delete from shopping_cart
            where id = #{cartId}
              and user_id = #{userId}
            """)
    int deleteByIdAndUserId(@Param("userId") Long userId,
                            @Param("cartId") Integer cartId);

    /**
     * 导航栏购物车角标只需要数量汇总，不需要拉完整购物车列表。
     */
    @Select("""
            select coalesce(sum(quantity), 0)
            from shopping_cart
            where user_id = #{userId}
            """)
    Integer countQuantityByUserId(@Param("userId") Long userId);

    /**
     * 批量勾选同样限制当前用户，避免前端传入别人的 cartId 时越权更新。
     */
    int updateSelectedBatch(@Param("userId") Long userId,
                            @Param("cartIds") List<Integer> cartIds,
                            @Param("selected") Integer selected);

    /**
     * 批量删除已选购物车项，返回影响行数用于校验是否存在无效 cartId。
     */
    int deleteBatchByUserId(@Param("userId") Long userId,
                            @Param("cartIds") List<Integer> cartIds);

    /**
     * 结算预览和下单复用的购物车明细查询，必须按当前用户过滤。
     */
    List<CartItemQueryObject> selectCartItemsByIds(@Param("userId") Long userId,
                                                   @Param("cartIds") List<Integer> cartIds);
}
