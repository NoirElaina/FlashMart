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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductDetailServiceImpl implements ProductDetailService {

    private static final String DETAIL_LOCK_PREFIX = "lock:product:detail:";

    @Autowired
    private ProductDetailMapper productDetailMapper;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductCacheService productCacheService;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public ProductDetailVO productDetail(Long productId) {
        // 1. 先读缓存：命中直接返回；命中空值标记直接 404（防穿透）。
        ProductDetailVO cached = readCacheOrThrow(productId);
        if (cached != null) {
            return cached;
        }

        // 2. 缓存未命中：加分布式锁，只放一个线程回源重建，其余线程等待后读缓存（防击穿）。
        RLock lock = redissonClient.getLock(DETAIL_LOCK_PREFIX + productId);
        boolean locked = false;
        try {
            locked = lock.tryLock(2, 10, TimeUnit.SECONDS);
            if (!locked) {
                // 没抢到锁，说明已有线程在重建；稍等后再读一次缓存。
                ProductDetailVO afterWait = readCacheOrThrow(productId);
                if (afterWait != null) {
                    return afterWait;
                }
                // 兜底：仍未就绪则自行回源，避免请求一直挂着。
                return loadFromDbAndCache(productId);
            }

            // 拿到锁后二次确认：可能在等待锁期间别的线程已经重建好。
            ProductDetailVO doubleCheck = readCacheOrThrow(productId);
            if (doubleCheck != null) {
                return doubleCheck;
            }
            return loadFromDbAndCache(productId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return loadFromDbAndCache(productId);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 读缓存：HIT 返回数据；ABSENT 抛 404；MISS 返回 null 交给上层回源。
     */
    private ProductDetailVO readCacheOrThrow(Long productId) {
        ProductDetailCacheResult lookup = productCacheService.lookupDetail(productId);
        if (lookup.status() == ProductDetailCacheResult.Status.HIT) {
            return lookup.value();
        }
        if (lookup.status() == ProductDetailCacheResult.Status.ABSENT) {
            throw new BusinessException(404, "商品不存在");
        }
        return null;
    }

    /**
     * 回源数据库并写缓存。持锁线程或兜底线程才会走到这里。
     */
    private ProductDetailVO loadFromDbAndCache(Long productId) {
        ProductDO productDO = productService.getById(productId);
        if (productDO == null) {
            // 确认不存在，写空值标记防穿透。
            productCacheService.cacheAbsent(productId);
            throw new BusinessException(404, "商品不存在");
        }

        ProductDetailDO productDetailDO = productDetailMapper.selectByProductId(productId);
        if (productDetailDO == null) {
            throw new BusinessException(404, "商品详情不存在");
        }

        List<ProductImageDO> productImageDOList = productDetailMapper.selectImageListByProductId(productId);
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
