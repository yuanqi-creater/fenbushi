package com.ticketing.inventory.service.impl;

import com.ticketing.common.exception.BusinessException;
import com.ticketing.inventory.service.InventoryShardingService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 库存分片服务实现
 * 通过分片 + 分段锁方式实现高性能库存操作
 */
@Slf4j
@Service
public class InventoryShardingServiceImpl implements InventoryShardingService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 每个分片的库存数量
    private static final int SHARD_SIZE = 1000;
    
    // 分片数量
    private static final int SHARD_COUNT = 500;

    // 库存锁超时时间
    private static final int LOCK_TIMEOUT_SECONDS = 3;

    /**
     * 初始化票种库存分片
     */
    @Override
    public void initializeInventoryShards(Long eventId, Long ticketTypeId, int totalQuantity) {
        try {
            // 计算需要的分片数量
            int requiredShards = (totalQuantity + SHARD_SIZE - 1) / SHARD_SIZE;
            
            // 确保不超过最大分片数
            if (requiredShards > SHARD_COUNT) {
                throw new BusinessException("库存数量超过系统上限");
            }

            // 初始化每个分片
            for (int i = 0; i < requiredShards; i++) {
                String shardKey = getInventoryShardKey(eventId, ticketTypeId, i);
                int shardQuantity = Math.min(SHARD_SIZE, totalQuantity - i * SHARD_SIZE);
                redisTemplate.opsForValue().set(shardKey, String.valueOf(shardQuantity));
            }

            // 记录分片数量
            String shardCountKey = getShardCountKey(eventId, ticketTypeId);
            redisTemplate.opsForValue().set(shardCountKey, String.valueOf(requiredShards));

            log.info("Initialized inventory shards for event: {}, ticketType: {}, total: {}, shards: {}",
                    eventId, ticketTypeId, totalQuantity, requiredShards);
        } catch (Exception e) {
            log.error("Failed to initialize inventory shards", e);
            throw new BusinessException("初始化库存分片失败");
        }
    }

    /**
     * 锁定库存
     * 使用分段锁和乐观锁保证并发安全
     */
    @Override
    public boolean lockStock(Long eventId, Long ticketTypeId, Long userId, int quantity) {
        List<InventoryLockResult> lockResults = new ArrayList<>();
        
        try {
            // 获取分片数量
            String shardCountKey = getShardCountKey(eventId, ticketTypeId);
            String shardCountStr = redisTemplate.opsForValue().get(shardCountKey);
            int shardCount = Integer.parseInt(shardCountStr);

            // 遍历分片尝试锁定库存
            int remainingQuantity = quantity;
            for (int i = 0; i < shardCount && remainingQuantity > 0; i++) {
                String shardKey = getInventoryShardKey(eventId, ticketTypeId, i);
                InventoryLockResult result = tryLockShardStock(shardKey, remainingQuantity);
                
                if (result.isSuccess()) {
                    lockResults.add(result);
                    remainingQuantity -= result.getLockedQuantity();
                }
            }

            // 检查是否完全锁定成功
            if (remainingQuantity > 0) {
                // 回滚已锁定的库存
                rollbackLocks(lockResults);
                return false;
            }

            // 记录用户锁定信息
            recordUserLock(eventId, ticketTypeId, userId, quantity);
            return true;
        } catch (Exception e) {
            log.error("Failed to lock stock", e);
            rollbackLocks(lockResults);
            return false;
        }
    }

    /**
     * 扣减库存
     * 将锁定的库存真正扣减
     */
    @Override
    public boolean deductStock(Long eventId, Long ticketTypeId, Long userId, int quantity) {
        try {
            // 验证之前的锁定记录
            if (!validateUserLock(eventId, ticketTypeId, userId, quantity)) {
                return false;
            }

            // 清除锁定记录
            clearUserLock(eventId, ticketTypeId, userId);
            return true;
        } catch (Exception e) {
            log.error("Failed to deduct stock", e);
            return false;
        }
    }

    /**
     * 释放库存
     * 将锁定的库存释放回分片
     */
    @Override
    public boolean releaseStock(Long eventId, Long ticketTypeId, Long userId, int quantity) {
        try {
            // 验证之前的锁定记录
            if (!validateUserLock(eventId, ticketTypeId, userId, quantity)) {
                return false;
            }

            // 获取用户锁定的分片信息并释放
            List<InventoryLockResult> lockResults = getUserLockResults(eventId, ticketTypeId, userId);
            for (InventoryLockResult result : lockResults) {
                releaseShardStock(result.getShardKey(), result.getLockedQuantity());
            }

            // 清除锁定记录
            clearUserLock(eventId, ticketTypeId, userId);
            return true;
        } catch (Exception e) {
            log.error("Failed to release stock", e);
            return false;
        }
    }

    /**
     * 尝试锁定分片库存
     */
    private InventoryLockResult tryLockShardStock(String shardKey, int requestQuantity) {
        RLock lock = redissonClient.getLock(shardKey + ":lock");
        
        try {
            // 获取分段锁
            if (!lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                return InventoryLockResult.failed(shardKey);
            }

            // 检查分片库存
            String quantityStr = redisTemplate.opsForValue().get(shardKey);
            int availableQuantity = Integer.parseInt(quantityStr);
            
            if (availableQuantity <= 0) {
                return InventoryLockResult.failed(shardKey);
            }

            // 计算实际可锁定数量
            int lockQuantity = Math.min(requestQuantity, availableQuantity);
            
            // 更新分片库存
            redisTemplate.opsForValue().set(shardKey, String.valueOf(availableQuantity - lockQuantity));
            
            return InventoryLockResult.success(shardKey, lockQuantity);
        } catch (Exception e) {
            log.error("Failed to lock shard stock", e);
            return InventoryLockResult.failed(shardKey);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 回滚已锁定的库存
     */
    private void rollbackLocks(List<InventoryLockResult> lockResults) {
        for (InventoryLockResult result : lockResults) {
            if (result.isSuccess()) {
                releaseShardStock(result.getShardKey(), result.getLockedQuantity());
            }
        }
    }

    /**
     * 释放分片库存
     */
    private void releaseShardStock(String shardKey, int quantity) {
        RLock lock = redissonClient.getLock(shardKey + ":lock");
        
        try {
            if (lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                String currentQuantityStr = redisTemplate.opsForValue().get(shardKey);
                int currentQuantity = Integer.parseInt(currentQuantityStr);
                redisTemplate.opsForValue().set(shardKey, String.valueOf(currentQuantity + quantity));
            }
        } catch (Exception e) {
            log.error("Failed to release shard stock", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 工具方法：生成库存分片key
    private String getInventoryShardKey(Long eventId, Long ticketTypeId, int shardIndex) {
        return String.format("inventory:shard:%d:%d:%d", eventId, ticketTypeId, shardIndex);
    }

    // 工具方法：生成分片数量key
    private String getShardCountKey(Long eventId, Long ticketTypeId) {
        return String.format("inventory:shard:count:%d:%d", eventId, ticketTypeId);
    }

    // 工具方法：记录用户锁定信息
    private void recordUserLock(Long eventId, Long ticketTypeId, Long userId, int quantity) {
        String userLockKey = String.format("inventory:lock:%d:%d:%d", eventId, ticketTypeId, userId);
        redisTemplate.opsForValue().set(userLockKey, String.valueOf(quantity), 10, TimeUnit.MINUTES);
    }

    // 工具方法：验证用户锁定记录
    private boolean validateUserLock(Long eventId, Long ticketTypeId, Long userId, int quantity) {
        String userLockKey = String.format("inventory:lock:%d:%d:%d", eventId, ticketTypeId, userId);
        String lockedQuantityStr = redisTemplate.opsForValue().get(userLockKey);
        return lockedQuantityStr != null && Integer.parseInt(lockedQuantityStr) == quantity;
    }

    // 工具方法：清除用户锁定记录
    private void clearUserLock(Long eventId, Long ticketTypeId, Long userId) {
        String userLockKey = String.format("inventory:lock:%d:%d:%d", eventId, ticketTypeId, userId);
        redisTemplate.delete(userLockKey);
    }

    // 工具方法：获取用户锁定的分片信息
    private List<InventoryLockResult> getUserLockResults(Long eventId, Long ticketTypeId, Long userId) {
        // 实际项目中需要从Redis或其他存储中获取用户锁定的具体分片信息
        return new ArrayList<>();
    }

    /**
     * 库存锁定结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor(staticName = "of")
    private static class InventoryLockResult {
        private String shardKey;
        private int lockedQuantity;
        private boolean success;

        public static InventoryLockResult success(String shardKey, int quantity) {
            return of(shardKey, quantity, true);
        }

        public static InventoryLockResult failed(String shardKey) {
            return of(shardKey, 0, false);
        }
    }
} 