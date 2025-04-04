package com.ticketing.ticket.service.impl;

import com.ticketing.common.entity.Event;
import com.ticketing.common.exception.BusinessException;
import com.ticketing.ticket.mapper.EventMapper;
import com.ticketing.ticket.service.EventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 场次服务实现类
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "event")
public class EventServiceImpl implements EventService {

    @Autowired
    private EventMapper eventMapper;

    // 场次状态常量
    public static final int STATUS_NOT_STARTED = 0;  // 未开始
    public static final int STATUS_ON_SALE = 1;      // 售票中
    public static final int STATUS_SALE_ENDED = 2;   // 售票结束
    public static final int STATUS_IN_PROGRESS = 3;  // 进行中
    public static final int STATUS_ENDED = 4;        // 已结束
    public static final int STATUS_CANCELLED = 5;    // 已取消

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(allEntries = true)
    public Long createEvent(Event event) {
        log.info("Creating new event: {}", event);
        try {
            // 参数校验
            validateEvent(event);

            // 设置初始状态和时间
            event.setStatus(STATUS_NOT_STARTED)
                    .setCreateTime(LocalDateTime.now())
                    .setUpdateTime(LocalDateTime.now());

            // 插入数据
            eventMapper.insert(event);
            log.info("Successfully created event with id: {}", event.getId());
            return event.getId();
        } catch (Exception e) {
            log.error("Failed to create event: {}", event, e);
            throw new BusinessException("创建场次失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "#event.id")
    public boolean updateEvent(Event event) {
        log.info("Updating event: {}", event);
        try {
            // 参数校验
            validateEvent(event);

            // 检查场次是否存在
            Event existingEvent = eventMapper.selectById(event.getId());
            if (existingEvent == null) {
                throw new BusinessException("场次不存在");
            }

            // 检查状态变更是否合法
            validateStatusTransition(existingEvent.getStatus(), event.getStatus());

            // 更新数据
            event.setUpdateTime(LocalDateTime.now());
            boolean success = eventMapper.updateById(event) > 0;
            log.info("Successfully updated event: {}", event.getId());
            return success;
        } catch (Exception e) {
            log.error("Failed to update event: {}", event, e);
            throw new BusinessException("更新场次失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "#eventId")
    public boolean deleteEvent(Long eventId) {
        log.info("Deleting event: {}", eventId);
        try {
            // 检查场次是否存在
            Event existingEvent = eventMapper.selectById(eventId);
            if (existingEvent == null) {
                throw new BusinessException("场次不存在");
            }

            // 检查场次状态
            if (existingEvent.getStatus() != STATUS_NOT_STARTED && existingEvent.getStatus() != STATUS_CANCELLED) {
                throw new BusinessException("只能删除未开始或已取消的场次");
            }

            boolean success = eventMapper.deleteById(eventId) > 0;
            log.info("Successfully deleted event: {}", eventId);
            return success;
        } catch (Exception e) {
            log.error("Failed to delete event: {}", eventId, e);
            throw new BusinessException("删除场次失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(key = "#eventId", unless = "#result == null")
    public Event getEvent(Long eventId) {
        log.info("Getting event: {}", eventId);
        try {
            Event event = eventMapper.selectById(eventId);
            // 自动更新场次状态
            if (event != null) {
                updateEventStatus(event);
            }
            return event;
        } catch (Exception e) {
            log.error("Failed to get event: {}", eventId, e);
            throw new BusinessException("获取场次失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(key = "'ongoing'", unless = "#result == null")
    public List<Event> getOngoingEvents() {
        log.info("Getting ongoing events");
        try {
            List<Event> events = eventMapper.selectOngoingEvents();
            // 更新每个场次的状态
            events.forEach(this::updateEventStatus);
            return events;
        } catch (Exception e) {
            log.error("Failed to get ongoing events", e);
            throw new BusinessException("获取进行中场次失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(key = "'upcoming'", unless = "#result == null")
    public List<Event> getUpcomingEvents() {
        log.info("Getting upcoming events");
        try {
            List<Event> events = eventMapper.selectUpcomingEvents();
            // 更新每个场次的状态
            events.forEach(this::updateEventStatus);
            return events;
        } catch (Exception e) {
            log.error("Failed to get upcoming events", e);
            throw new BusinessException("获取即将开始场次失败: " + e.getMessage());
        }
    }

    /**
     * 校验场次参数
     */
    private void validateEvent(Event event) {
        if (event.getStartTime() == null) {
            throw new BusinessException("活动开始时间不能为空");
        }
        if (event.getEndTime() == null) {
            throw new BusinessException("活动结束时间不能为空");
        }
        if (event.getSaleStartTime() == null) {
            throw new BusinessException("售票开始时间不能为空");
        }
        if (event.getSaleEndTime() == null) {
            throw new BusinessException("售票结束时间不能为空");
        }
        if (event.getStartTime().isAfter(event.getEndTime())) {
            throw new BusinessException("活动开始时间不能晚于结束时间");
        }
        if (event.getSaleStartTime().isAfter(event.getSaleEndTime())) {
            throw new BusinessException("售票开始时间不能晚于结束时间");
        }
        if (event.getSaleEndTime().isAfter(event.getStartTime())) {
            throw new BusinessException("售票必须在活动开始前结束");
        }
    }

    /**
     * 更新场次状态
     */
    private void updateEventStatus(Event event) {
        LocalDateTime now = LocalDateTime.now();
        int newStatus = event.getStatus();

        if (event.getStatus() == STATUS_CANCELLED) {
            return;
        }

        if (now.isBefore(event.getSaleStartTime())) {
            newStatus = STATUS_NOT_STARTED;
        } else if (now.isBefore(event.getSaleEndTime())) {
            newStatus = STATUS_ON_SALE;
        } else if (now.isBefore(event.getStartTime())) {
            newStatus = STATUS_SALE_ENDED;
        } else if (now.isBefore(event.getEndTime())) {
            newStatus = STATUS_IN_PROGRESS;
        } else {
            newStatus = STATUS_ENDED;
        }

        if (newStatus != event.getStatus()) {
            event.setStatus(newStatus);
            event.setUpdateTime(now);
            eventMapper.updateById(event);
            log.info("Updated event status: eventId={}, newStatus={}", event.getId(), newStatus);
        }
    }

    /**
     * 校验状态变更是否合法
     */
    private void validateStatusTransition(int currentStatus, int newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        // 只允许将未开始的场次变更为已取消
        if (newStatus == STATUS_CANCELLED && currentStatus != STATUS_NOT_STARTED) {
            throw new BusinessException("只能取消未开始的场次");
        }

        // 不允许手动变更到其他状态
        if (newStatus != STATUS_CANCELLED) {
            throw new BusinessException("场次状态会自动更新，不允许手动变更");
        }
    }
} 