package com.ticketing.inventory.controller;

import com.ticketing.common.entity.Inventory;
import com.ticketing.common.response.Result;
import com.ticketing.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 库存控制器
 */
@Slf4j
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    /**
     * 初始化库存
     */
    @PostMapping("/initialize")
    public Result<Boolean> initializeInventory(@RequestParam("eventId") Long eventId,
                                             @RequestParam("ticketTypeId") Long ticketTypeId,
                                             @RequestParam("totalStock") Integer totalStock) {
        log.info("Initialize inventory: eventId={}, ticketTypeId={}, totalStock={}", eventId, ticketTypeId, totalStock);
        boolean success = inventoryService.initializeInventory(eventId, ticketTypeId, totalStock);
        return Result.success(success);
    }

    /**
     * 锁定库存
     */
    @PostMapping("/lock")
    public Result<Boolean> lockStock(@RequestParam("eventId") Long eventId,
                                   @RequestParam("ticketTypeId") Long ticketTypeId,
                                   @RequestParam("userId") Long userId,
                                   @RequestParam("quantity") Integer quantity) {
        log.info("Lock stock: eventId={}, ticketTypeId={}, userId={}, quantity={}", eventId, ticketTypeId, userId, quantity);
        boolean success = inventoryService.lockStock(eventId, ticketTypeId, userId, quantity);
        return Result.success(success);
    }

    /**
     * 释放库存
     */
    @PostMapping("/release")
    public Result<Boolean> releaseStock(@RequestParam("eventId") Long eventId,
                                      @RequestParam("ticketTypeId") Long ticketTypeId,
                                      @RequestParam("userId") Long userId,
                                      @RequestParam("quantity") Integer quantity) {
        log.info("Release stock: eventId={}, ticketTypeId={}, userId={}, quantity={}", eventId, ticketTypeId, userId, quantity);
        boolean success = inventoryService.releaseStock(eventId, ticketTypeId, userId, quantity);
        return Result.success(success);
    }

    /**
     * 扣减库存
     */
    @PostMapping("/deduct")
    public Result<Boolean> deductStock(@RequestParam("eventId") Long eventId,
                                     @RequestParam("ticketTypeId") Long ticketTypeId,
                                     @RequestParam("userId") Long userId,
                                     @RequestParam("quantity") Integer quantity) {
        log.info("Deduct stock: eventId={}, ticketTypeId={}, userId={}, quantity={}", eventId, ticketTypeId, userId, quantity);
        boolean success = inventoryService.deductStock(eventId, ticketTypeId, userId, quantity);
        return Result.success(success);
    }

    /**
     * 查询库存
     */
    @GetMapping("/query")
    public Result<Inventory> getInventory(@RequestParam("eventId") Long eventId,
                                        @RequestParam("ticketTypeId") Long ticketTypeId) {
        log.info("Query inventory: eventId={}, ticketTypeId={}", eventId, ticketTypeId);
        Inventory inventory = inventoryService.getInventory(eventId, ticketTypeId);
        return Result.success(inventory);
    }

    /**
     * 查询分片库存
     */
    @GetMapping("/query/sharding")
    public Result<Inventory> getInventoryBySharding(@RequestParam("eventId") Long eventId,
                                                  @RequestParam("ticketTypeId") Long ticketTypeId,
                                                  @RequestParam("shardingId") Integer shardingId) {
        log.info("Query sharding inventory: eventId={}, ticketTypeId={}, shardingId={}", eventId, ticketTypeId, shardingId);
        Inventory inventory = inventoryService.getInventoryBySharding(eventId, ticketTypeId, shardingId);
        return Result.success(inventory);
    }
} 