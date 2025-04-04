package com.ticketing.common.utils;

import org.springframework.stereotype.Component;

/**
 * 分片工具类
 */
@Component
public class ShardingUtil {

    /**
     * 默认分片数量
     */
    private static final int DEFAULT_SHARDING_COUNT = 20;

    /**
     * 计算分片ID
     * @param userId 用户ID
     * @param eventId 场次ID
     * @return 分片ID
     */
    public int getShardingId(Long userId, Long eventId) {
        // 使用用户ID和场次ID的组合作为分片依据
        long shardingKey = (userId * 31 + eventId) % DEFAULT_SHARDING_COUNT;
        return (int) (Math.abs(shardingKey) % DEFAULT_SHARDING_COUNT);
    }

    /**
     * 获取分片数量
     * @return 分片数量
     */
    public int getShardingCount() {
        return DEFAULT_SHARDING_COUNT;
    }

    /**
     * 计算每个分片的初始库存
     * @param totalStock 总库存
     * @return 每个分片的库存
     */
    public int getShardingStock(int totalStock) {
        // 向上取整，确保总库存足够
        return (totalStock + DEFAULT_SHARDING_COUNT - 1) / DEFAULT_SHARDING_COUNT;
    }

    /**
     * 获取数据库分片索引
     * @param eventId 场次ID
     * @return 数据库分片索引
     */
    public int getDatabaseShardingIndex(Long eventId) {
        // 使用场次ID作为分片依据，假设有16个物理库
        return (int) (Math.abs(eventId) % 16);
    }
} 