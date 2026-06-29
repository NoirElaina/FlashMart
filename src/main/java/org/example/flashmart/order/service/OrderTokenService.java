package org.example.flashmart.order.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * 下单幂等令牌：进结算页时发一个一次性 token，下单时校验并消费。
 * 用 Redis DEL 的原子性保证同一 token 只能被消费一次，从而拦住重复提交。
 */
@Service
public class OrderTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String TOKEN_KEY_PREFIX = "order:idempotent:";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(10);

    public OrderTokenService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String issue() {
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, "1", TOKEN_TTL);
        return token;
    }

    /**
     * 校验并消费 token。DEL 是原子操作，并发下只有一个请求能删成功（返回 true）。
     */
    public boolean consume(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.delete(TOKEN_KEY_PREFIX + token));
    }
}
