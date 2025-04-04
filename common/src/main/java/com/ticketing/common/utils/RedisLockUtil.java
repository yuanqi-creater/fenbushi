package com.ticketing.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁工具类
 */
@Slf4j
@Component
public class RedisLockUtil {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 获取分段锁的key
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param shardingId 分片ID
     * @return 锁key
     */
    public String getSegmentLockKey(Long eventId, Long ticketTypeId, Integer shardingId) {
        return String.format("inventory:lock:%d:%d:%d", eventId, ticketTypeId, shardingId);
    }

    /**
     * 尝试获取分布式锁
     * @param lockKey 锁key
     * @param waitTime 等待时间
     * @param leaseTime 持有锁的时间
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        try {
            RLock lock = redissonClient.getLock(lockKey);
            return lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("Try lock failed, key: {}", lockKey, e);
            return false;
        }
    }

    /**
     * 释放分布式锁
     * @param lockKey 锁key
     */
    public void unlock(String lockKey) {
        try {
            RLock lock = redissonClient.getLock(lockKey);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("Unlock failed, key: {}", lockKey, e);
        }
    }

    /**
     * 获取分布式锁并执行任务
     * @param lockKey 锁key
     * @param waitTime 等待时间
     * @param leaseTime 持有锁的时间
     * @param task 要执行的任务
     * @return 任务执行结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, LockTask<T> task) {
        boolean locked = false;
        try {
            locked = tryLock(lockKey, waitTime, leaseTime);
            if (locked) {
                return task.execute();
            }
            throw new RuntimeException("Failed to acquire lock: " + lockKey);
        } finally {
            if (locked) {
                unlock(lockKey);
            }
        }
    }

    /**
     * 锁任务接口
     */
    @FunctionalInterface
    public interface LockTask<T> {
        T execute();
    }
} 