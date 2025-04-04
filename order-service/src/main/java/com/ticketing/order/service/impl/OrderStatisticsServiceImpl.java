package com.ticketing.order.service.impl;

import com.ticketing.order.mapper.OrderMapper;
import com.ticketing.order.service.OrderStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单统计服务实现类
 */
@Slf4j
@Service
public class OrderStatisticsServiceImpl implements OrderStatisticsService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    @Cacheable(value = "order:statistics", key = "#startTime.toString() + '-' + #endTime.toString()")
    public Map<String, Object> getOrderStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Getting order statistics from {} to {}", startTime, endTime);
        try {
            Map<String, Object> result = orderMapper.selectOrderStatistics(startTime, endTime);
            log.info("Order statistics result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get order statistics", e);
            return new HashMap<>();
        }
    }

    @Override
    @Cacheable(value = "order:ticket:sales", key = "#eventId")
    public List<Map<String, Object>> getTicketTypeSalesStatistics(Long eventId) {
        log.info("Getting ticket type sales statistics for event: {}", eventId);
        try {
            List<Map<String, Object>> result = orderMapper.selectTicketTypeSalesStatistics(eventId);
            log.info("Ticket type sales statistics result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get ticket type sales statistics", e);
            return List.of();
        }
    }

    @Override
    @Cacheable(value = "order:user:statistics", key = "#userId")
    public Map<String, Object> getUserOrderStatistics(Long userId) {
        log.info("Getting user order statistics for user: {}", userId);
        try {
            Map<String, Object> result = orderMapper.selectUserOrderStatistics(userId);
            log.info("User order statistics result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get user order statistics", e);
            return new HashMap<>();
        }
    }

    @Override
    @Cacheable(value = "order:hot:tickets", key = "#startTime.toString() + '-' + #endTime.toString() + '-' + #limit")
    public List<Map<String, Object>> getHotTicketTypes(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        log.info("Getting hot ticket types from {} to {}, limit: {}", startTime, endTime, limit);
        try {
            List<Map<String, Object>> result = orderMapper.selectHotTicketTypes(startTime, endTime, limit);
            log.info("Hot ticket types result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get hot ticket types", e);
            return List.of();
        }
    }

    @Override
    @Cacheable(value = "order:hourly:statistics", key = "#date.toString()")
    public List<Map<String, Object>> getHourlyOrderStatistics(LocalDateTime date) {
        log.info("Getting hourly order statistics for date: {}", date);
        try {
            List<Map<String, Object>> result = orderMapper.selectHourlyOrderStatistics(date);
            log.info("Hourly order statistics result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get hourly order statistics", e);
            return List.of();
        }
    }

    @Override
    @Cacheable(value = "order:refund:statistics", key = "#startTime.toString() + '-' + #endTime.toString()")
    public Map<String, Object> getRefundStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Getting refund statistics from {} to {}", startTime, endTime);
        try {
            Map<String, Object> result = orderMapper.selectRefundStatistics(startTime, endTime);
            log.info("Refund statistics result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Failed to get refund statistics", e);
            return new HashMap<>();
        }
    }
} 