package com.ticketing.inventory.service;

import com.ticketing.inventory.model.EventInventory.TicketType;
import java.util.List;
import java.util.Map;

/**
 * 座位库存服务
 */
public interface SeatInventoryService {

    /**
     * 初始化场次座位库存
     *
     * @param eventId 场次ID
     * @param seatMap 座位分布图（区域->座位列表）
     * @param ticketTypes 票价类型
     * @return 是否成功
     */
    boolean initializeSeatInventory(Long eventId, Map<String, List<String>> seatMap, List<TicketType> ticketTypes);

    /**
     * 锁定座位
     *
     * @param eventId 场次ID
     * @param seatIds 座位ID列表
     * @param userId 用户ID
     * @param lockDuration 锁定时长（秒）
     * @return 是否成功
     */
    boolean lockSeats(Long eventId, List<String> seatIds, Long userId, int lockDuration);

    /**
     * 释放座位
     *
     * @param eventId 场次ID
     * @param seatIds 座位ID列表
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean releaseSeats(Long eventId, List<String> seatIds, Long userId);

    /**
     * 确认座位预订
     *
     * @param eventId 场次ID
     * @param seatIds 座位ID列表
     * @param userId 用户ID
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean confirmSeats(Long eventId, List<String> seatIds, Long userId, Long orderId);

    /**
     * 查询座位状态
     *
     * @param eventId 场次ID
     * @param seatIds 座位ID列表
     * @return 座位状态映射（座位ID->状态）
     */
    Map<String, String> querySeatStatus(Long eventId, List<String> seatIds);

    /**
     * 获取可用座位
     *
     * @param eventId 场次ID
     * @param quantity 需要的座位数量
     * @param ticketTypeId 票价类型ID
     * @return 可用座位ID列表
     */
    List<String> getAvailableSeats(Long eventId, int quantity, Long ticketTypeId);

    /**
     * 批量更新座位状态
     *
     * @param eventId 场次ID
     * @param seatStatusMap 座位状态映射（座位ID->状态）
     * @return 是否成功
     */
    boolean batchUpdateSeatStatus(Long eventId, Map<String, String> seatStatusMap);
} 