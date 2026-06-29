package org.example.flashmart.product.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.flashmart.common.exception.BusinessException;
import org.example.flashmart.product.cache.ProductCacheService;
import org.example.flashmart.product.cache.ProductDetailCacheResult;
import org.example.flashmart.product.mapper.ProductDetailMapper;
import org.example.flashmart.product.model.dataobject.ProductDO;
import org.example.flashmart.product.model.dataobject.ProductDetailDO;
import org.example.flashmart.product.model.dataobject.ProductImageDO;
import org.example.flashmart.product.model.dataobject.ProductReviewDO;
import org.example.flashmart.product.model.vo.ProductDetailVO;
import org.example.flashmart.product.model.vo.ReviewSummaryVO;
import org.example.flashmart.product.model.vo.ReviewVO;
import org.example.flashmart.product.model.vo.SeckillVO;
import org.example.flashmart.product.model.vo.StockVO;
import org.example.flashmart.product.service.ProductDetailService;
import org.example.flashmart.product.service.ProductService;
import org.example.flashmart.user.mapper.UserMapper;
import org.example.flashmart.user.model.dataobject.UserDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductDetailServiceImpl implements ProductDetailService {
    @Autowired
    private ProductDetailMapper productDetailMapper;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductCacheService productCacheService;

    @Override
    public ProductDetailVO productDetail(Long productId) {
        ProductDetailCacheResult lookup = productCacheService.lookupDetail(productId);
        if (lookup.status() == ProductDetailCacheResult.Status.HIT) {
            return lookup.value();
        }
        if (lookup.status() == ProductDetailCacheResult.Status.ABSENT) {
            // 命中空值标记：这个 id 之前已确认不存在，直接 404，不再打库。
            throw new BusinessException(404, "商品不存在");
        }

        ProductDO productDO = productService.getById(productId);
        if (productDO == null) {
            // 回源后确认商品不存在，写空值标记，防止同一非法 id 反复穿透到数据库。
            productCacheService.cacheAbsent(productId);
            throw new BusinessException(404, "商品不存在");
        }

        ProductDetailDO productDetailDO = productDetailMapper.selectByProductId(productId);
        if (productDetailDO == null) {
            throw new BusinessException(404, "商品详情不存在");
        }

        List<ProductImageDO> productImageDOList = productDetailMapper.selectImageListByProductId(productId);
        if (productImageDOList.isEmpty()) {
            log.info("productImageDOList is empty");
        }
        log.info("productImageDOList size:{}", productImageDOList.size());
        List<ProductReviewDO> productReviewDOList = productDetailMapper.selectReviewListByProductId(productId);

        ProductDetailVO productDetailVO = new ProductDetailVO()
                .setId(productDO.getId())
                .setName(productDO.getName())
                .setCategory(productDO.getCategory())
                .setOriginalPrice(productDO.getOriginalPrice())
                .setSalePrice(productDO.getSalePrice())
                .setSold(productDO.getSold())
                .setIsHot(Integer.valueOf(1).equals(productDO.getHot()))
                .setLimitPerUser(productDO.getLimitPerUser())
                .setDescription(productDetailDO.getDescription())
                .setSpecs(productDetailDO.getSpecs())
                .setMainImage(
                        productImageDOList.stream()
                                .filter(img -> Integer.valueOf(1).equals(img.getIsMain()))
                                .map(ProductImageDO::getUrl)
                                .findFirst()
                                .orElse(null)
                )
                .setImages(
                        productImageDOList.stream()
                                .map(ProductImageDO::getUrl)
                                .collect(Collectors.toList())
                )
                .setSeckill(buildSeckill(productDO))
                .setReviewSummary(buildReviewSummary(productReviewDOList));

        productCacheService.cacheDetail(productId, productDetailVO);
        return productDetailVO;
    }

    private ReviewSummaryVO buildReviewSummary(List<ProductReviewDO> productReviewDOList) {
        ReviewSummaryVO reviewSummaryVO = new ReviewSummaryVO();
        reviewSummaryVO.setAvgRating(productReviewDOList.stream()
                .mapToInt(ProductReviewDO::getRating)
                .average()
                .orElse(0.0));
        reviewSummaryVO.setTotalCount(productReviewDOList.size());

        Map<Integer, Integer> distribution = productReviewDOList.stream()
                .collect(Collectors.groupingBy(
                        ProductReviewDO::getRating,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        reviewSummaryVO.setDistribution(distribution);

        List<ReviewVO> latestReviews = productReviewDOList.stream()
                .sorted(Comparator.comparing(ProductReviewDO::getCreateTime).reversed())
                .limit(5)
                .map(review -> {
                    UserDO userDO = userMapper.selectById(review.getUserId());
                    ReviewVO reviewVO = new ReviewVO();
                    reviewVO.setId(review.getId());
                    reviewVO.setRating(review.getRating());
                    reviewVO.setContent(review.getContent());
                    reviewVO.setImages(review.getImages() != null ? review.getImages() : Collections.emptyList());
                    reviewVO.setCreateTime(review.getCreateTime());
                    reviewVO.setNickname(userDO != null ? userDO.getUsername() : "用户" + review.getUserId());
                    reviewVO.setAvatar(null);
                    return reviewVO;
                })
                .collect(Collectors.toList());
        reviewSummaryVO.setLatestReviews(latestReviews);

        return reviewSummaryVO;
    }

    private SeckillVO buildSeckill(ProductDO productDO) {
        if (!Boolean.TRUE.equals(productDO.getSeckill())) {
            return null;
        }

        if (productDO.getSeckillStartTime() == null || productDO.getSeckillEndTime() == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isActive = !now.isBefore(productDO.getSeckillStartTime()) && !now.isAfter(productDO.getSeckillEndTime());
        String statusText;
        if (now.isBefore(productDO.getSeckillStartTime())) {
            statusText = "秒杀即将开始";
        } else if (now.isAfter(productDO.getSeckillEndTime())) {
            statusText = "秒杀已结束";
        } else {
            statusText = "秒杀进行中";
        }

        StockVO stockVO = new StockVO();
        boolean available = productDO.getStock() != null && productDO.getStock() > 0;
        stockVO.setAvailable(available);
        stockVO.setDisplayText(available ? "库存充足" : "暂时缺货");

        SeckillVO seckillVO = new SeckillVO();
        seckillVO.setStartTime(productDO.getSeckillStartTime());
        seckillVO.setEndTime(productDO.getSeckillEndTime());
        seckillVO.setIsActive(isActive);
        seckillVO.setStatusText(statusText);
        seckillVO.setStock(stockVO);
        seckillVO.setCountdownSeconds(null);
        return seckillVO;
    }

}
