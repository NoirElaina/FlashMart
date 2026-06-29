package org.example.flashmart.product.service.impl;

import org.example.flashmart.common.exception.BusinessException;
import org.example.flashmart.common.response.PageResult;
import org.example.flashmart.product.cache.ProductCacheService;
import org.example.flashmart.product.mapper.ProductMapper;
import org.example.flashmart.product.model.dataobject.ProductDO;
import org.example.flashmart.product.model.query.ProductQuery;
import org.example.flashmart.product.model.vo.ProductVO;
import org.example.flashmart.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductCacheService productCacheService;

    @Override
    public PageResult<ProductVO> pageProducts(ProductQuery productQuery) {
        int currentPage = (productQuery.getPage() == null || productQuery.getPage() < 1) ? 1 : productQuery.getPage();
        int size = (productQuery.getPageSize() == null || productQuery.getPageSize() < 1) ? 10 : productQuery.getPageSize();
        int offset = (currentPage - 1) * size;
        String category = productQuery.getCategory();
        String normalizedCategory = (category == null || category.isBlank() || "全部".equals(category)) ? null : category;
        List<ProductVO> products = productMapper.pageProducts(offset, size, normalizedCategory)
                .stream()
                .map(this::toVO)
                .toList();
        long total = productMapper.countProducts(normalizedCategory);
        return new PageResult<>(products, total, currentPage, size);
    }

    @Override
    public ProductDO getById(Long productId) {
        return productMapper.selectById(productId);
    }

    @Override
    public void updatePrice(Long productId, BigDecimal originalPrice, BigDecimal salePrice) {
        // 单条 UPDATE 自带原子性，更新成功（已落库）后再删缓存，符合 Cache Aside 的先更库再删缓存顺序。
        int updated = productMapper.updatePrice(productId, originalPrice, salePrice);
        if (updated == 0) {
            throw new BusinessException(404, "商品不存在");
        }
        evictDetailCache(productId);
    }

    @Override
    public void updateStatus(Long productId, Integer status) {
        int updated = productMapper.updateStatus(productId, status);
        if (updated == 0) {
            throw new BusinessException(404, "商品不存在");
        }
        evictDetailCache(productId);
    }

    /**
     * Cache Aside + 延迟双删：立即删一次，500ms 后再删一次，防止并发读把旧值回填。
     */
    private void evictDetailCache(Long productId) {
        productCacheService.evictDetail(productId);
        productCacheService.evictDetailDelayed(productId);
    }

    private ProductVO toVO(ProductDO product) {
        return new ProductVO()
                .setId(product.getId())
                .setName(product.getName())
                .setImage(product.getImage())
                .setCategory(product.getCategory())
                .setOriginalPrice(product.getOriginalPrice())
                .setSalePrice(product.getSalePrice())
                .setStock(product.getStock())
                .setSold(product.getSold())
                .setLimitPerUser(product.getLimitPerUser())
                .setHot(product.getHot())
                .setSeckill(product.getSeckill())
                .setStatus(product.getStatus())
                .setSeckillStartTime(product.getSeckillStartTime())
                .setSeckillEndTime(product.getSeckillEndTime());
    }
}
