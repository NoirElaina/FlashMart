package org.example.flashmart.seckill.service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillStockService {

    public static final long RESULT_ALREADY_BOUGHT = -2;
    public static final long RESULT_SOLD_OUT = -1;

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String BOUGHT_KEY_PREFIX = "seckill:bought:";

    private final StringRedisTemplate stringRedisTemplate;
    private DefaultRedisScript<Long> seckillScript;

    public SeckillStockService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostConstruct
    public void init() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/seckill.lua")));
        seckillScript.setResultType(Long.class);
    }

    /**
     * 活动上线时把库存预热进 Redis；重新预热会清空已购集合。
     */
    public void preheat(Long activityId, int stock) {
        stringRedisTemplate.opsForValue().set(stockKey(activityId), String.valueOf(stock));
        stringRedisTemplate.delete(boughtKey(activityId));
    }

    /**
     * Lua 原子预减：一人一单校验 + 库存判断 + 扣减一步完成。
     * 返回 -2 已抢过 / -1 已售罄 / >=0 剩余库存。
     */
    public long tryDeduct(Long activityId, Long userId) {
        Long result = stringRedisTemplate.execute(
                seckillScript,
                List.of(stockKey(activityId), boughtKey(activityId)),
                String.valueOf(userId)
        );
        return result == null ? RESULT_SOLD_OUT : result;
    }

    /**
     * 后续 DB 扣减失败时回补 Redis：库存 +1 并移出已购集合，避免少卖。
     */
    public void restore(Long activityId, Long userId) {
        stringRedisTemplate.opsForValue().increment(stockKey(activityId));
        stringRedisTemplate.opsForSet().remove(boughtKey(activityId), String.valueOf(userId));
    }

    public Long getStock(Long activityId) {
        String value = stringRedisTemplate.opsForValue().get(stockKey(activityId));
        return value == null ? null : Long.valueOf(value);
    }

    private String stockKey(Long activityId) {
        return STOCK_KEY_PREFIX + activityId;
    }

    private String boughtKey(Long activityId) {
        return BOUGHT_KEY_PREFIX + activityId;
    }
}
