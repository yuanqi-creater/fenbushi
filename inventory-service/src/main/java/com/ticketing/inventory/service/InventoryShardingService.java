package com.ticketing.inventory.service;

/**
 * 库存分片服务接口
 * 通过分片方式管理库存，提供高性能的库存操作
 */
public interface InventoryShardingService {

    /**
     * 初始化票种库存分片
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param totalQuantity 总库存数量
     */
    void initializeInventoryShards(Long eventId, Long ticketTypeId, int totalQuantity);

    /**
     * 锁定库存
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param userId 用户ID
     * @param quantity 锁定数量
     * @return 是否锁定成功
     */
    boolean lockStock(Long eventId, Long ticketTypeId, Long userId, int quantity);

    /**
     * 扣减库存
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param userId 用户ID
     * @param quantity 扣减数量
     * @return 是否扣减成功
     */
    boolean deductStock(Long eventId, Long ticketTypeId, Long userId, int quantity);

    /**
     * 释放库存
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param userId 用户ID
     * @param quantity 释放数量
     * @return 是否释放成功
     */
    boolean releaseStock(Long eventId, Long ticketTypeId, Long userId, int quantity);
} 