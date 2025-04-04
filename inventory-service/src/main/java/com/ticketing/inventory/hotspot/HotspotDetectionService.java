package com.ticketing.inventory.hotspot;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 热点数据探测服务
 * 基于Flink实时分析Redis访问日志，识别热点票务
 */
@Slf4j
@Service
public class HotspotDetectionService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 本地缓存
    private Cache<String, InventoryInfo> localCache;

    // 热点商品访问计数器
    private Map<String, HotspotCounter> hotspotCounters = new ConcurrentHashMap<>();

    // 热点阈值
    private static final int HOTSPOT_THRESHOLD = 1000; // 每分钟访问次数超过1000次判定为热点
    private static final int CACHE_EXPIRE_MINUTES = 30; // 本地缓存过期时间

    @PostConstruct
    public void init() {
        // 初始化本地缓存
        localCache = Caffeine.newBuilder()
                .expireAfterWrite(CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
                .maximumSize(10000)
                .build();

        // 启动Flink任务
        initializeFlinkJob();
    }

    /**
     * 初始化Flink实时分析任务
     */
    private void initializeFlinkJob() {
        try {
            StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

            // 读取Redis访问日志流
            DataStream<AccessLog> accessLogStream = env.addSource(new RedisAccessLogSource());

            // 按商品ID分组，统计访问次数
            accessLogStream
                    .keyBy(AccessLog::getInventoryKey)
                    .timeWindow(Time.minutes(1))
                    .process(new HotspotDetectionFunction())
                    .addSink(new HotspotNotificationSink());

            env.execute("Hotspot Detection Job");
        } catch (Exception e) {
            log.error("Failed to initialize Flink job", e);
        }
    }

    /**
     * 获取库存信息，优先从本地缓存获取
     */
    public InventoryInfo getInventoryInfo(String inventoryKey) {
        // 先查本地缓存
        InventoryInfo info = localCache.getIfPresent(inventoryKey);
        if (info != null) {
            return info;
        }

        // 本地缓存未命中，查Redis
        String quantity = redisTemplate.opsForValue().get(inventoryKey);
        if (quantity != null) {
            info = new InventoryInfo(Integer.parseInt(quantity));
            
            // 如果是热点商品，放入本地缓存
            if (isHotspot(inventoryKey)) {
                localCache.put(inventoryKey, info);
                log.info("Added to local cache: {}", inventoryKey);
            }
            
            return info;
        }

        return null;
    }

    /**
     * 更新库存信息
     */
    public void updateInventoryInfo(String inventoryKey, int quantity) {
        // 更新Redis
        redisTemplate.opsForValue().set(inventoryKey, String.valueOf(quantity));
        
        // 如果是热点商品，同时更新本地缓存
        if (isHotspot(inventoryKey)) {
            localCache.put(inventoryKey, new InventoryInfo(quantity));
        }
    }

    /**
     * 判断是否为热点商品
     */
    private boolean isHotspot(String inventoryKey) {
        HotspotCounter counter = hotspotCounters.get(inventoryKey);
        return counter != null && counter.getAccessCount() >= HOTSPOT_THRESHOLD;
    }

    /**
     * 热点检测处理函数
     */
    private class HotspotDetectionFunction 
            extends ProcessWindowFunction<AccessLog, HotspotEvent, String, TimeWindow> {
        
        @Override
        public void process(String key, Context context, Iterable<AccessLog> elements,
                          Collector<HotspotEvent> out) {
            
            long count = 0;
            for (AccessLog log : elements) {
                count++;
            }

            if (count >= HOTSPOT_THRESHOLD) {
                // 发现热点商品
                HotspotEvent event = new HotspotEvent(key, count);
                out.collect(event);
                
                // 更新计数器
                hotspotCounters.put(key, new HotspotCounter(count));
                
                log.info("Detected hotspot: {}, access count: {}", key, count);
            }
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class AccessLog {
        private String inventoryKey;
        private long timestamp;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class HotspotEvent {
        private String inventoryKey;
        private long accessCount;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class InventoryInfo {
        private int quantity;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class HotspotCounter {
        private long accessCount;
    }
} 