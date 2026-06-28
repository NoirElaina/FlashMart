package org.example.flashmart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class FlashMartApplicationTests {
    @Autowired
    private StringRedisTemplate  stringRedisTemplate;
    @Test
    void contextLoads() {
        stringRedisTemplate.opsForList().leftPush("orderList", "1");
    }

}
