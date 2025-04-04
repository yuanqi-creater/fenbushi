package com.ticketing.inventory.config;

import com.google.common.collect.Range;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 库存分片路由配置
 * 基于用户ID和场次ID的两级分片策略
 */
@Configuration
public class ShardingConfig {

    // 物理库数量
    private static final int PHYSICAL_DB_COUNT = 16;
    
    // 每个物理库的分片数量
    private static final int SHARDS_PER_DB = 20;

    /**
     * 库存分片算法
     * 基于用户ID和场次ID的复合分片
     */
    public static class InventoryShardingAlgorithm implements ComplexKeysShardingAlgorithm<Long> {
        
        @Override
        public Collection<String> doSharding(Collection<String> availableTargetNames, 
                                           ComplexKeysShardingValue<Long> shardingValue) {
            
            Map<String, Collection<Long>> columnNameAndShardingValuesMap = shardingValue.getColumnNameAndShardingValuesMap();
            Collection<Long> userIds = columnNameAndShardingValuesMap.get("user_id");
            Collection<Long> eventIds = columnNameAndShardingValuesMap.get("event_id");

            List<String> result = new ArrayList<>();
            
            for (Long userId : userIds) {
                for (Long eventId : eventIds) {
                    // 计算物理库索引
                    int dbIndex = calculateDatabaseIndex(userId, eventId);
                    // 计算分片索引
                    int shardIndex = calculateShardIndex(userId, eventId);
                    
                    String target = String.format("inventory_db_%d.inventory_shard_%d", dbIndex, shardIndex);
                    result.add(target);
                }
            }
            
            return result;
        }

        /**
         * 计算物理库索引
         * 使用用户ID和场次ID的组合hash
         */
        private int calculateDatabaseIndex(Long userId, Long eventId) {
            long combinedHash = (userId * 31 + eventId) % PHYSICAL_DB_COUNT;
            return Math.abs((int)combinedHash);
        }

        /**
         * 计算分片索引
         * 使用场次ID作为主要分片依据
         */
        private int calculateShardIndex(Long userId, Long eventId) {
            return Math.abs((int)(eventId % SHARDS_PER_DB));
        }
    }

    /**
     * 分片表配置
     */
    public static class ShardingTableRuleConfig {
        public static final String LOGIC_TABLE = "t_inventory";
        public static final String ACTUAL_DATA_NODES = "inventory_db_${0..15}.inventory_shard_${0..19}";
        
        // 分片键
        public static final String DATABASE_SHARDING_COLUMN = "user_id";
        public static final String TABLE_SHARDING_COLUMN = "event_id";
        
        // 分片算法
        public static final ComplexKeysShardingAlgorithm<Long> SHARDING_ALGORITHM = new InventoryShardingAlgorithm();
    }

    /**
     * 动态扩容配置
     * 支持在线添加新的物理库
     */
    public static class DynamicExpansionConfig {
        
        /**
         * 计算新库的路由规则
         */
        public static Range<Long> calculateNewDatabaseRange(int newDbIndex, int totalDbs) {
            long rangeSize = Long.MAX_VALUE / totalDbs;
            long rangeStart = rangeSize * newDbIndex;
            long rangeEnd = rangeStart + rangeSize;
            return Range.closed(rangeStart, rangeEnd);
        }
        
        /**
         * 生成扩容后的分片规则
         */
        public static String generateExpandedRule(int currentDbCount, int newDbCount) {
            return String.format("inventory_db_${%d..%d}.inventory_shard_${0..%d}",
                    currentDbCount, newDbCount - 1, SHARDS_PER_DB - 1);
        }
    }
} 