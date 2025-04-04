package com.ticketing.ticket.service;

import com.ticketing.common.entity.Event;

import java.util.List;

/**
 * 场次服务接口
 */
public interface EventService {

    /**
     * 创建场次
     * @param event 场次信息
     * @return 场次ID
     */
    Long createEvent(Event event);

    /**
     * 更新场次
     * @param event 场次信息
     * @return 是否成功
     */
    boolean updateEvent(Event event);

    /**
     * 删除场次
     * @param eventId 场次ID
     * @return 是否成功
     */
    boolean deleteEvent(Long eventId);

    /**
     * 获取场次信息
     * @param eventId 场次ID
     * @return 场次信息
     */
    Event getEvent(Long eventId);

    /**
     * 获取所有进行中的场次
     * @return 场次列表
     */
    List<Event> getOngoingEvents();

    /**
     * 获取所有即将开始的场次
     * @return 场次列表
     */
    List<Event> getUpcomingEvents();
} 