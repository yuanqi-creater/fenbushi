package com.ticketing.common.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 二级缓存工具类
 */
@Slf4j
@Component
public class CacheUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 本地缓存，默认最大容量1000，过期时间5分钟
    private final Cache<String, Object> localCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    /**
     * 从二级缓存中获取数据
     * @param key 缓存key
     * @param supplier 数据提供者
     * @param redisExpireTime Redis缓存过期时间（秒）
     * @return 缓存数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getFromCache(String key, Supplier<T> supplier, long redisExpireTime) {
        // 1. 先从本地缓存获取
        Object localValue = localCache.getIfPresent(key);
        if (localValue != null) {
            return (T) localValue;
        }

        // 2. 从Redis缓存获取
        Object redisValue = redisTemplate.opsForValue().get(key);
        if (redisValue != null) {
            // 放入本地缓存
            localCache.put(key, redisValue);
            return (T) redisValue;
        }

        // 3. 从数据源获取
        T value = supplier.get();
        if (value != null) {
            // 放入Redis缓存
            redisTemplate.opsForValue().set(key, value, redisExpireTime, TimeUnit.SECONDS);
            // 放入本地缓存
            localCache.put(key, value);
        }
        return value;
    }

    /**
     * 删除缓存
     * @param key 缓存key
     */
    public void deleteCache(String key) {
        // 删除本地缓存
        localCache.invalidate(key);
        // 删除Redis缓存
        redisTemplate.delete(key);
    }

    /**
     * 更新缓存
     * @param key 缓存key
     * @param value 新值
     * @param redisExpireTime Redis缓存过期时间（秒）
     */
    public void updateCache(String key, Object value, long redisExpireTime) {
        if (value != null) {
            // 更新Redis缓存
            redisTemplate.opsForValue().set(key, value, redisExpireTime, TimeUnit.SECONDS);
            // 更新本地缓存
            localCache.put(key, value);
        }
    }

    /**
     * 获取库存缓存key
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param shardingId 分片ID
     * @return 缓存key
     */
    public String getInventoryCacheKey(Long eventId, Long ticketTypeId, Integer shardingId) {
        return String.format("inventory:stock:%d:%d:%d", eventId, ticketTypeId, shardingId);
    }
} 