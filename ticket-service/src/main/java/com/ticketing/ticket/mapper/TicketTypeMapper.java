package com.ticketing.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketing.common.entity.TicketType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 票种Mapper接口
 */
@Mapper
public interface TicketTypeMapper extends BaseMapper<TicketType> {
    
    /**
     * 根据场次ID查询票种列表
     *
     * @param eventId 场次ID
     * @return 票种列表
     */
    @Select("SELECT * FROM ticket_type WHERE event_id = #{eventId}")
    List<TicketType> selectByEventId(Long eventId);
} 