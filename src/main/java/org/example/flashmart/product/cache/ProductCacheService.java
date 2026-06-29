package org.example.flashmart.product.cache;

import lombok.extern.slf4j.Slf4j;
import org.example.flashmart.product.model.vo.ProductDetailVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class ProductCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PRODUCT_DETAIL_KEY_PREFIX = "flashmart:product:detail:";

    // 空值标记：商品不存在时缓存它，防止同一非法 id 反复穿透到数据库。短 TTL，避免长时间挡住后来真上架的商品。
    private static final String NULL_MARKER = "\u0000NULL\u0000";
    private static final Duration NULL_TTL = Duration.ofMinutes(1);

    public ProductCacheService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public ProductDetailCacheResult lookupDetail(Long productId) {
        String key = PRODUCT_DETAIL_KEY_PREFIX + productId;
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null || value.isEmpty()) {
            return ProductDetailCacheResult.miss();
        }
        if (NULL_MARKER.equals(value)) {
            return ProductDetailCacheResult.absent();
        }
        try {
            return ProductDetailCacheResult.hit(objectMapper.readValue(value, ProductDetailVO.class));
        } catch (Exception e) {
            // 反序列化失败一般是 VO 结构变更，剔除脏缓存回源，避免缓存毒化导致接口 500。
            log.warn("商品详情缓存反序列化失败，剔除脏缓存并回源，productId={}", productId, e);
            stringRedisTemplate.delete(key);
            return ProductDetailCacheResult.miss();
        }
    }

    public void cacheDetail(Long productId, ProductDetailVO productDetailVO) {
        String key = PRODUCT_DETAIL_KEY_PREFIX + productId;
        try {
            String json = objectMapper.writeValueAsString(productDetailVO);
            // 加 5~10 分钟随机抖动，避免缓存集中失效引发雪崩。
            long jitterMinutes = ThreadLocalRandom.current().nextLong(5, 11);
            stringRedisTemplate.opsForValue().set(key, json, Duration.ofMinutes(30).plusMinutes(jitterMinutes));
        } catch (Exception e) {
            log.warn("商品详情缓存写入失败，productId={}", productId, e);
        }
    }

    /**
     * 商品不存在时写入空值标记，挡住后续相同非法 id 的请求（防缓存穿透）。
     */
    public void cacheAbsent(Long productId) {
        stringRedisTemplate.opsForValue().set(PRODUCT_DETAIL_KEY_PREFIX + productId, NULL_MARKER, NULL_TTL);
    }
}
