package org.example.flashmart.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.flashmart.product.model.dataobject.ProductDetailDO;
import org.example.flashmart.product.model.dataobject.ProductImageDO;
import org.example.flashmart.product.model.dataobject.ProductReviewDO;

import java.util.List;

@Mapper
public interface ProductDetailMapper {

    /**
     * 查询商品图文详情和规格参数，主商品基础信息由 ProductMapper 负责。
     */
    @Select("select id, product_id as productId,description,specs as specs,create_time as createTime,update_time as updateTime from product_detail where product_id = #{productId}")
    ProductDetailDO selectByProductId(Long productId);

    /**
     * 商品图片按 sort 升序返回，前端用第一张或 isMain 图做默认展示。
     */
    @Select("select id, product_id as productId,url,sort,is_main as isMain,create_time as createTime from product_images where product_id = #{productId} order by sort asc")
    List<ProductImageDO> selectImageListByProductId(Long productId);

    /**
     * 只返回可见评论，避免后台隐藏评论继续出现在商品详情页。
     */
    @Select("select id, product_id as productId,user_id as userId,order_id as orderId,rating,content,images as images,is_visible as isVisible,create_time as createTime,update_time as updateTime from product_reviews where product_id = #{productId} and is_visible = 1 order by create_time desc")
    List<ProductReviewDO> selectReviewListByProductId(Long productId);
}
