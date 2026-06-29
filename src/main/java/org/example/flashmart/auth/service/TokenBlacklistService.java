package org.example.flashmart.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String BLACKLIST_KEY_PREFIX = "flashmart:auth:blacklist:";

    public TokenBlacklistService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 登出时把 token 的 jti 拉黑，过期时间设为 token 的剩余有效期，到期后自动清理。
     */
    public void blacklist(String jti, long ttlMillis) {
        if (jti == null || ttlMillis <= 0) {
            return;
        }
        stringRedisTemplate.opsForValue().set(BLACKLIST_KEY_PREFIX + jti, "1", Duration.ofMillis(ttlMillis));
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_KEY_PREFIX + jti));
    }
}
