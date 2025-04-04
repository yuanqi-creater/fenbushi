package com.ticketing.inventory.service.impl;

import com.ticketing.common.entity.Inventory;
import com.ticketing.common.exception.BusinessException;
import com.ticketing.common.utils.CacheUtil;
import com.ticketing.common.utils.RedisLockUtil;
import com.ticketing.common.utils.ShardingUtil;
import com.ticketing.inventory.mapper.InventoryMapper;
import com.ticketing.inventory.service.InventoryService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 库存服务实现类
 */
@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private ShardingUtil shardingUtil;

    @Autowired
    private RedisLockUtil redisLockUtil;

    @Autowired
    private CacheUtil cacheUtil;

    private static final long LOCK_WAIT_TIME = 1000L;
    private static final long LOCK_LEASE_TIME = 5000L;
    private static final long CACHE_EXPIRE_TIME = 300L;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initializeInventory(Long eventId, Long ticketTypeId, Integer totalStock) {
        // 计算每个分片的初始库存
        int shardingStock = shardingUtil.getShardingStock(totalStock);
        int shardingCount = shardingUtil.getShardingCount();

        // 初始化每个分片的库存
        for (int i = 0; i < shardingCount; i++) {
            Inventory inventory = new Inventory()
                    .setEventId(eventId)
                    .setTicketTypeId(ticketTypeId)
                    .setShardingId(i)
                    .setTotalStock(shardingStock)
                    .setAvailableStock(shardingStock)
                    .setSoldStock(0)
                    .setLockedStock(0)
                    .setVersion(0);
            
            inventoryMapper.insert(inventory);
            
            // 更新缓存
            String cacheKey = cacheUtil.getInventoryCacheKey(eventId, ticketTypeId, i);
            cacheUtil.updateCache(cacheKey, inventory, CACHE_EXPIRE_TIME);
        }
        return true;
    }

    @Override
    @GlobalTransactional
    public boolean lockStock(Long eventId, Long ticketTypeId, Long userId, Integer quantity) {
        // 计算用户对应的分片ID
        int shardingId = shardingUtil.getShardingId(userId, eventId);
        
        // 获取分布式锁
        String lockKey = redisLockUtil.getSegmentLockKey(eventId, ticketTypeId, shardingId);
        return redisLockUtil.executeWithLock(lockKey, LOCK_WAIT_TIME, LOCK_LEASE_TIME, () -> {
            // 查询分片库存
            Inventory inventory = getInventoryBySharding(eventId, ticketTypeId, shardingId);
            if (inventory == null || inventory.getAvailableStock() < quantity) {
                throw new BusinessException("库存不足");
            }

            // 更新库存
            inventory.setAvailableStock(inventory.getAvailableStock() - quantity)
                    .setLockedStock(inventory.getLockedStock() + quantity);
            
            int updated = inventoryMapper.updateStock(inventory);
            if (updated <= 0) {
                throw new BusinessException("库存更新失败");
            }

            // 更新缓存
            String cacheKey = cacheUtil.getInventoryCacheKey(eventId, ticketTypeId, shardingId);
            cacheUtil.deleteCache(cacheKey);
            
            return true;
        });
    }

    @Override
    @GlobalTransactional
    public boolean releaseStock(Long eventId, Long ticketTypeId, Long userId, Integer quantity) {
        // 计算用户对应的分片ID
        int shardingId = shardingUtil.getShardingId(userId, eventId);
        
        // 获取分布式锁
        String lockKey = redisLockUtil.getSegmentLockKey(eventId, ticketTypeId, shardingId);
        return redisLockUtil.executeWithLock(lockKey, LOCK_WAIT_TIME, LOCK_LEASE_TIME, () -> {
            // 查询分片库存
            Inventory inventory = getInventoryBySharding(eventId, ticketTypeId, shardingId);
            if (inventory == null || inventory.getLockedStock() < quantity) {
                throw new BusinessException("锁定库存不足");
            }

            // 更新库存
            inventory.setAvailableStock(inventory.getAvailableStock() + quantity)
                    .setLockedStock(inventory.getLockedStock() - quantity);
            
            int updated = inventoryMapper.updateStock(inventory);
            if (updated <= 0) {
                throw new BusinessException("库存更新失败");
            }

            // 更新缓存
            String cacheKey = cacheUtil.getInventoryCacheKey(eventId, ticketTypeId, shardingId);
            cacheUtil.deleteCache(cacheKey);
            
            return true;
        });
    }

    @Override
    @GlobalTransactional
    public boolean deductStock(Long eventId, Long ticketTypeId, Long userId, Integer quantity) {
        // 计算用户对应的分片ID
        int shardingId = shardingUtil.getShardingId(userId, eventId);
        
        // 获取分布式锁
        String lockKey = redisLockUtil.getSegmentLockKey(eventId, ticketTypeId, shardingId);
        return redisLockUtil.executeWithLock(lockKey, LOCK_WAIT_TIME, LOCK_LEASE_TIME, () -> {
            // 查询分片库存
            Inventory inventory = getInventoryBySharding(eventId, ticketTypeId, shardingId);
            if (inventory == null || inventory.getLockedStock() < quantity) {
                throw new BusinessException("锁定库存不足");
            }

            // 更新库存
            inventory.setLockedStock(inventory.getLockedStock() - quantity)
                    .setSoldStock(inventory.getSoldStock() + quantity);
            
            int updated = inventoryMapper.updateStock(inventory);
            if (updated <= 0) {
                throw new BusinessException("库存更新失败");
            }

            // 更新缓存
            String cacheKey = cacheUtil.getInventoryCacheKey(eventId, ticketTypeId, shardingId);
            cacheUtil.deleteCache(cacheKey);
            
            return true;
        });
    }

    @Override
    public Inventory getInventory(Long eventId, Long ticketTypeId) {
        List<Inventory> inventories = inventoryMapper.selectByEventAndTicketType(eventId, ticketTypeId);
        if (inventories == null || inventories.isEmpty()) {
            return null;
        }

        // 汇总所有分片的库存
        Inventory total = new Inventory()
                .setEventId(eventId)
                .setTicketTypeId(ticketTypeId)
                .setTotalStock(0)
                .setAvailableStock(0)
                .setSoldStock(0)
                .setLockedStock(0);

        for (Inventory inventory : inventories) {
            total.setTotalStock(total.getTotalStock() + inventory.getTotalStock())
                    .setAvailableStock(total.getAvailableStock() + inventory.getAvailableStock())
                    .setSoldStock(total.getSoldStock() + inventory.getSoldStock())
                    .setLockedStock(total.getLockedStock() + inventory.getLockedStock());
        }

        return total;
    }

    @Override
    public Inventory getInventoryBySharding(Long eventId, Long ticketTypeId, Integer shardingId) {
        String cacheKey = cacheUtil.getInventoryCacheKey(eventId, ticketTypeId, shardingId);
        return cacheUtil.getFromCache(cacheKey,
                () -> inventoryMapper.selectBySharding(eventId, ticketTypeId, shardingId),
                CACHE_EXPIRE_TIME);
    }
} 