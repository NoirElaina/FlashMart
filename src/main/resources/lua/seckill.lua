-- KEYS[1] = 库存 key (seckill:stock:{activityId})
-- KEYS[2] = 已购用户集合 key (seckill:bought:{activityId})
-- ARGV[1] = userId
-- 返回: -2 已经抢过 / -1 库存不足 / >=0 预减后剩余库存
if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then
    return -2
end
local stock = tonumber(redis.call('GET', KEYS[1]))
if stock == nil or stock <= 0 then
    return -1
end
redis.call('DECR', KEYS[1])
redis.call('SADD', KEYS[2], ARGV[1])
return stock - 1
