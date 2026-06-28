package org.example.flashmart.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.example.flashmart.order.model.dataobject.OrderDO;
import org.example.flashmart.order.model.dataobject.OrderItemDO;
import org.example.flashmart.order.model.vo.OrderDetailVO;
import org.example.flashmart.order.model.vo.OrderItemVO;

import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 条件扣库存：同时校验库存和上架状态，避免超卖或下架商品继续下单。
     */
    @Update("update products set stock=stock-#{quantity}, sold=sold+#{quantity} ,update_time=now() where id=#{productId} and stock>=#{quantity} and status=1")
    int deductStock(@Param("productId") Integer productId, @Param("quantity") Integer quantity);

    /**
     * 插入订单主表，XML 中 useGeneratedKeys 会把自增 ID 回填到 order.id。
     */
    void insertOrder(OrderDO order);

    /**
     * 批量插入订单商品快照，订单项要保存下单时的名称、图片和价格。
     */
    void insertOrderItems(@Param("items") List<OrderItemDO> items);

    /**
     * 查询当前用户订单列表，必须按 userId 隔离，避免用户看到别人的订单。
     */
    List<OrderDetailVO> listOrders(@Param("userId") Long userId);

    /**
     * 查询订单详情主表信息，userId + orderId 双条件用于防止越权访问。
     */
    OrderDetailVO selectOrderDetail(@Param("userId") Long userId, @Param("orderId") Long orderId);

    /**
     * 查询订单商品快照，展示历史订单时不再回查商品表当前价格。
     */
    List<OrderItemVO> selectOrderItems(@Param("orderId") Long orderId);

    /**
     * 只允许取消当前用户的待支付订单，已支付或已取消的订单不能重复取消。
     */
    int cancelOrder(@Param("userId") Long userId, @Param("orderId") Long orderId);

    /**
     * 支付成功落库必须同时校验待支付状态和支付截止时间，防止超时订单被并发支付成功。
     */
    int payOrder(@Param("userId") Long userId, @Param("orderId") Long orderId);

    /**
     * 取消订单后回补库存，sold 校验用于避免异常数据导致销量被扣成负数。
     */
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

    List<OrderItemVO> selectOrderItemsByOrderIds(@Param("orderIds") List<Long> orderIds);
}
