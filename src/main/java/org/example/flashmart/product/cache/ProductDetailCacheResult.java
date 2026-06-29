package org.example.flashmart.product.cache;

import org.example.flashmart.product.model.vo.ProductDetailVO;

/**
 * 商品详情缓存查询结果，区分三种状态：
 * <ul>
 *     <li>{@link Status#MISS} 缓存未命中，需要回源数据库</li>
 *     <li>{@link Status#ABSENT} 命中空值标记，商品确定不存在，直接 404，不打库（防穿透）</li>
 *     <li>{@link Status#HIT} 命中真实数据</li>
 * </ul>
 */
public record ProductDetailCacheResult(Status status, ProductDetailVO value) {

    public enum Status {
        MISS,
        ABSENT,
        HIT
    }

    public static ProductDetailCacheResult miss() {
        return new ProductDetailCacheResult(Status.MISS, null);
    }

    public static ProductDetailCacheResult absent() {
        return new ProductDetailCacheResult(Status.ABSENT, null);
    }

    public static ProductDetailCacheResult hit(ProductDetailVO value) {
        return new ProductDetailCacheResult(Status.HIT, value);
    }
}
