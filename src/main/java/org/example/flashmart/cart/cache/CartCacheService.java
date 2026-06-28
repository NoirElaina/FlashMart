package org.example.flashmart.cart.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CartCacheService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String CART_COUNT_KEY_PREFIX = "flashmart:cart:count:";

    public CartCacheService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Integer getCartCount(Long userId) {
        String value = stringRedisTemplate.opsForValue().get(CART_COUNT_KEY_PREFIX + userId);
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void putCartCount(Long userId, Integer count) {
        stringRedisTemplate.opsForValue().set(CART_COUNT_KEY_PREFIX + userId, String.valueOf(count), Duration.ofMinutes(10));
    }

    public void evictCartCount(Long userId) {
        stringRedisTemplate.delete(CART_COUNT_KEY_PREFIX + userId);
    }
}
