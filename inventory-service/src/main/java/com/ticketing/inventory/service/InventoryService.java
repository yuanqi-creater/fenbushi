package com.ticketing.inventory.service;

import com.ticketing.common.entity.Inventory;

/**
 * 库存服务接口
 */
public interface InventoryService {

    /**
     * 初始化库存
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param totalStock 总库存
     * @return 是否成功
     */
    boolean initializeInventory(Long eventId, Long ticketTypeId, Integer totalStock);

    /**
     * 锁定库存
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param userId 用户ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean lockStock(Long eventId, Long ticketTypeId, Long userId, Integer quantity);

    /**
     * 释放库存
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param userId 用户ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean releaseStock(Long eventId, Long ticketTypeId, Long userId, Integer quantity);

    /**
     * 扣减库存
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param userId 用户ID
     * @param quantity 数量
     * @return 是否成功
     */
    boolean deductStock(Long eventId, Long ticketTypeId, Long userId, Integer quantity);

    /**
     * 查询库存
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @return 库存信息
     */
    Inventory getInventory(Long eventId, Long ticketTypeId);

    /**
     * 查询分片库存
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param shardingId 分片ID
     * @return 库存信息
     */
    Inventory getInventoryBySharding(Long eventId, Long ticketTypeId, Integer shardingId);
} 