package com.ticketing.ticket.service;

import com.ticketing.common.entity.TicketType;

import java.util.List;

/**
 * 票种服务接口
 */
public interface TicketTypeService {

    /**
     * 创建票种
     * @param ticketType 票种信息
     * @return 票种ID
     */
    Long createTicketType(TicketType ticketType);

    /**
     * 更新票种
     * @param ticketType 票种信息
     * @return 是否成功
     */
    boolean updateTicketType(TicketType ticketType);

    /**
     * 删除票种
     * @param ticketTypeId 票种ID
     * @return 是否成功
     */
    boolean deleteTicketType(Long ticketTypeId);

    /**
     * 获取票种信息
     * @param ticketTypeId 票种ID
     * @return 票种信息
     */
    TicketType getTicketType(Long ticketTypeId);

    /**
     * 获取场次下的所有票种
     * @param eventId 场次ID
     * @return 票种列表
     */
    List<TicketType> getTicketTypesByEvent(Long eventId);
} 