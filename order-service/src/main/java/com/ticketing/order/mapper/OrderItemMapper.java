package com.ticketing.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketing.common.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单项Mapper接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询订单项
     * @param orderId 订单ID
     * @return 订单项信息
     */
    @Select("SELECT * FROM t_order_item WHERE order_id = #{orderId} AND deleted = 0")
    OrderItem selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 统计订单项数量
     * @param orderId 订单ID
     * @return 订单项数量
     */
    @Select("SELECT COUNT(*) FROM t_order_item WHERE order_id = #{orderId} AND deleted = 0")
    int countByOrderId(@Param("orderId") Long orderId);

    /**
     * 统计用户在指定票种的购买数量
     * @param userId 用户ID
     * @param ticketTypeId 票种ID
     * @return 购买数量
     */
    @Select("SELECT COALESCE(SUM(oi.quantity), 0) " +
            "FROM t_order_item oi " +
            "JOIN t_order o ON oi.order_id = o.id " +
            "WHERE o.user_id = #{userId} " +
            "AND oi.ticket_type_id = #{ticketTypeId} " +
            "AND o.status IN (0, 1) " +  // 待支付或已支付状态
            "AND o.deleted = 0 " +
            "AND oi.deleted = 0")
    int sumUserTicketTypeQuantity(@Param("userId") Long userId, @Param("ticketTypeId") Long ticketTypeId);
} 