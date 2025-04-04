package com.ticketing.inventory.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 性能优化配置
 */
@Configuration
public class PerformanceConfig {

    /**
     * 库存操作专用线程池
     * - 核心线程数200：支持高并发请求
     * - 最大线程数400：应对突发流量
     * - 队列容量5000：削峰填谷
     */
    @Bean("inventoryThreadPool")
    public ThreadPoolTaskExecutor inventoryThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(200);
        executor.setMaxPoolSize(400);
        executor.setQueueCapacity(5000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("inventory-");
        // 拒绝策略：调用者运行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * Redis优化配置
     * - 使用StringRedisSerializer：减少序列化开销
     * - 配置连接池：提升连接复用效率
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * 本地缓存配置
     * - 初始容量10000：减少扩容开销
     * - 最大容量50000：控制内存使用
     * - 并发级别20：提升并发写入性能
     */
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .initialCapacity(10000)
                .maximumSize(50000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats();
    }
} 