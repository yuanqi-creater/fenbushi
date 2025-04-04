package com.ticketing.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketing.common.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 库存Mapper接口
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 更新库存
     * @param inventory 库存信息
     * @return 更新行数
     */
    int updateStock(@Param("inventory") Inventory inventory);

    /**
     * 查询指定场次和票种的所有分片库存
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @return 库存列表
     */
    List<Inventory> selectByEventAndTicketType(@Param("eventId") Long eventId, @Param("ticketTypeId") Long ticketTypeId);

    /**
     * 查询指定分片的库存
     * @param eventId 场次ID
     * @param ticketTypeId 票种ID
     * @param shardingId 分片ID
     * @return 库存信息
     */
    Inventory selectBySharding(@Param("eventId") Long eventId, @Param("ticketTypeId") Long ticketTypeId, @Param("shardingId") Integer shardingId);
} 