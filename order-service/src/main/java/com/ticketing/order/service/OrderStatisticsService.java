package com.ticketing.order.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单统计服务接口
 */
public interface OrderStatisticsService {

    /**
     * 统计指定时间范围内的订单总数和金额
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return Map包含订单总数(orderCount)和总金额(totalAmount)
     */
    Map<String, Object> getOrderStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计指定场次的票种销售情况
     * @param eventId 场次ID
     * @return Map包含票种ID和对应的销售数量、金额
     */
    List<Map<String, Object>> getTicketTypeSalesStatistics(Long eventId);

    /**
     * 获取用户消费统计
     * @param userId 用户ID
     * @return Map包含订单总数、总金额、平均订单金额等信息
     */
    Map<String, Object> getUserOrderStatistics(Long userId);

    /**
     * 获取热门票种排行
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 返回数量
     * @return 热门票种列表，包含销量和金额信息
     */
    List<Map<String, Object>> getHotTicketTypes(LocalDateTime startTime, LocalDateTime endTime, int limit);

    /**
     * 获取每小时订单统计
     * @param date 统计日期
     * @return 每小时的订单数量和金额
     */
    List<Map<String, Object>> getHourlyOrderStatistics(LocalDateTime date);

    /**
     * 获取退款率统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return Map包含总订单数、退款订单数、退款率等信息
     */
    Map<String, Object> getRefundStatistics(LocalDateTime startTime, LocalDateTime endTime);
} 