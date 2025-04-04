package com.ticketing.ticket.service.impl;

import com.ticketing.common.entity.Event;
import com.ticketing.common.entity.TicketType;
import com.ticketing.common.exception.BusinessException;
import com.ticketing.common.service.InventoryFeignClient;
import com.ticketing.ticket.mapper.EventMapper;
import com.ticketing.ticket.mapper.TicketTypeMapper;
import com.ticketing.ticket.service.TicketTypeService;
import io.seata.spring.annotation.GlobalTransactional;
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
 * 票种服务实现类
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "ticket_type")
public class TicketTypeServiceImpl implements TicketTypeService {

    @Autowired
    private TicketTypeMapper ticketTypeMapper;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private InventoryFeignClient inventoryFeignClient;

    @Override
    @GlobalTransactional
    @CacheEvict(allEntries = true)
    public Long createTicketType(TicketType ticketType) {
        log.info("Creating new ticket type: {}", ticketType);
        try {
            // 参数校验
            validateTicketType(ticketType);

            // 检查场次是否存在
            Event event = eventMapper.selectById(ticketType.getEventId());
            if (event == null) {
                throw new BusinessException("场次不存在");
            }

            // 检查场次状态
            if (event.getStatus() != EventServiceImpl.STATUS_NOT_STARTED) {
                throw new BusinessException("只能为未开始的场次创建票种");
            }

            // 设置创建时间
            ticketType.setCreateTime(LocalDateTime.now())
                    .setUpdateTime(LocalDateTime.now());

            // 插入数据
            ticketTypeMapper.insert(ticketType);

            // 初始化库存
            boolean success = inventoryFeignClient.initializeInventory(
                    ticketType.getEventId(),
                    ticketType.getId(),
                    ticketType.getTotalStock()
            );

            if (!success) {
                throw new BusinessException("初始化库存失败");
            }

            log.info("Successfully created ticket type with id: {}", ticketType.getId());
            return ticketType.getId();
        } catch (Exception e) {
            log.error("Failed to create ticket type: {}", ticketType, e);
            throw new BusinessException("创建票种失败: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional
    @CacheEvict(key = "#ticketType.id")
    public boolean updateTicketType(TicketType ticketType) {
        log.info("Updating ticket type: {}", ticketType);
        try {
            // 参数校验
            validateTicketType(ticketType);

            // 检查票种是否存在
            TicketType existingTicketType = ticketTypeMapper.selectById(ticketType.getId());
            if (existingTicketType == null) {
                throw new BusinessException("票种不存在");
            }

            // 检查场次状态
            Event event = eventMapper.selectById(ticketType.getEventId());
            if (event == null) {
                throw new BusinessException("场次不存在");
            }
            if (event.getStatus() != EventServiceImpl.STATUS_NOT_STARTED) {
                throw new BusinessException("只能修改未开始场次的票种");
            }

            // 检查库存变更
            if (!ticketType.getTotalStock().equals(existingTicketType.getTotalStock())) {
                throw new BusinessException("不允许修改总库存");
            }

            // 更新数据
            ticketType.setUpdateTime(LocalDateTime.now());
            boolean success = ticketTypeMapper.updateById(ticketType) > 0;
            log.info("Successfully updated ticket type: {}", ticketType.getId());
            return success;
        } catch (Exception e) {
            log.error("Failed to update ticket type: {}", ticketType, e);
            throw new BusinessException("更新票种失败: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional
    @CacheEvict(key = "#ticketTypeId")
    public boolean deleteTicketType(Long ticketTypeId) {
        log.info("Deleting ticket type: {}", ticketTypeId);
        try {
            // 检查票种是否存在
            TicketType ticketType = ticketTypeMapper.selectById(ticketTypeId);
            if (ticketType == null) {
                throw new BusinessException("票种不存在");
            }

            // 检查场次状态
            Event event = eventMapper.selectById(ticketType.getEventId());
            if (event == null) {
                throw new BusinessException("场次不存在");
            }
            if (event.getStatus() != EventServiceImpl.STATUS_NOT_STARTED) {
                throw new BusinessException("只能删除未开始场次的票种");
            }

            boolean success = ticketTypeMapper.deleteById(ticketTypeId) > 0;
            log.info("Successfully deleted ticket type: {}", ticketTypeId);
            return success;
        } catch (Exception e) {
            log.error("Failed to delete ticket type: {}", ticketTypeId, e);
            throw new BusinessException("删除票种失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(key = "#ticketTypeId", unless = "#result == null")
    public TicketType getTicketType(Long ticketTypeId) {
        log.info("Getting ticket type: {}", ticketTypeId);
        try {
            return ticketTypeMapper.selectById(ticketTypeId);
        } catch (Exception e) {
            log.error("Failed to get ticket type: {}", ticketTypeId, e);
            throw new BusinessException("获取票种失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(key = "'event:' + #eventId", unless = "#result == null")
    public List<TicketType> getTicketTypesByEvent(Long eventId) {
        log.info("Getting ticket types by event: {}", eventId);
        try {
            return ticketTypeMapper.selectByEventId(eventId);
        } catch (Exception e) {
            log.error("Failed to get ticket types by event: {}", eventId, e);
            throw new BusinessException("获取场次票种失败: " + e.getMessage());
        }
    }

    /**
     * 校验票种参数
     */
    private void validateTicketType(TicketType ticketType) {
        if (ticketType.getEventId() == null) {
            throw new BusinessException("场次ID不能为空");
        }
        if (ticketType.getName() == null || ticketType.getName().trim().isEmpty()) {
            throw new BusinessException("票种名称不能为空");
        }
        if (ticketType.getPrice() == null || ticketType.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException("票价必须大于0");
        }
        if (ticketType.getLimitPerOrder() == null || ticketType.getLimitPerOrder() <= 0) {
            throw new BusinessException("单次限购数量必须大于0");
        }
        if (ticketType.getTotalStock() == null || ticketType.getTotalStock() <= 0) {
            throw new BusinessException("总库存必须大于0");
        }
    }
} 