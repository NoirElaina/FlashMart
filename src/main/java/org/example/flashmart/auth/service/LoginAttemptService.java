package org.example.flashmart.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LoginAttemptService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String FAIL_KEY_PREFIX = "flashmart:auth:loginfail:";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_WINDOW = Duration.ofMinutes(15);

    public LoginAttemptService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean isLocked(String account) {
        String value = stringRedisTemplate.opsForValue().get(FAIL_KEY_PREFIX + account);
        if (value == null) {
            return false;
        }
        try {
            return Integer.parseInt(value) >= MAX_ATTEMPTS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void recordFailure(String account) {
        String key = FAIL_KEY_PREFIX + account;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        // 第一次失败时给计数器设置滑动窗口过期时间。
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, LOCK_WINDOW);
        }
    }

    public void reset(String account) {
        stringRedisTemplate.delete(FAIL_KEY_PREFIX + account);
    }
}
