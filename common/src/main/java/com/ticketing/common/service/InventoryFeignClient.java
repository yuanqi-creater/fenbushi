package com.ticketing.common.service;

import com.ticketing.common.response.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存服务Feign客户端
 */
@FeignClient(name = "inventory-service")
public interface InventoryFeignClient {

    /**
     * 初始化库存
     *
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param totalStock 总库存
     * @return 是否成功
     */
    @PostMapping("/inventory/initialize")
    Result<Boolean> initializeInventory(@RequestParam("eventId") Long eventId,
                                      @RequestParam("ticketTypeId") Long ticketTypeId,
                                      @RequestParam("totalStock") Integer totalStock);

    /**
     * 锁定库存
     *
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param userId 用户ID
     * @param quantity 数量
     * @return 是否成功
     */
    @PostMapping("/inventory/lock")
    Result<Boolean> lockStock(@RequestParam("eventId") Long eventId,
                            @RequestParam("ticketTypeId") Long ticketTypeId,
                            @RequestParam("userId") Long userId,
                            @RequestParam("quantity") Integer quantity);

    /**
     * 释放库存
     *
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param userId 用户ID
     * @param quantity 数量
     * @return 是否成功
     */
    @PostMapping("/inventory/release")
    Result<Boolean> releaseStock(@RequestParam("eventId") Long eventId,
                               @RequestParam("ticketTypeId") Long ticketTypeId,
                               @RequestParam("userId") Long userId,
                               @RequestParam("quantity") Integer quantity);

    /**
     * 扣减库存
     *
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param userId 用户ID
     * @param quantity 数量
     * @return 是否成功
     */
    @PostMapping("/inventory/deduct")
    Result<Boolean> deductStock(@RequestParam("eventId") Long eventId,
                              @RequestParam("ticketTypeId") Long ticketTypeId,
                              @RequestParam("userId") Long userId,
                              @RequestParam("quantity") Integer quantity);
} 