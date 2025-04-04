package com.ticketing.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketing.common.entity.Order;
import com.ticketing.common.vo.OrderDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单主表Mapper接口
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 查询用户订单列表
     * @param userId 用户ID
     * @param status 订单状态（可选）
     * @param offset 偏移量
     * @param size 每页大小
     * @return 订单列表
     */
    @Select({
        "<script>",
        "SELECT o.*, oi.ticket_type_id, oi.quantity, oi.unit_price",
        "FROM t_order o",
        "LEFT JOIN t_order_item oi ON o.id = oi.order_id",
        "WHERE o.user_id = #{userId}",
        "<if test='status != null'>",
        "  AND o.status = #{status}",
        "</if>",
        "AND o.deleted = 0",
        "ORDER BY o.create_time DESC",
        "LIMIT #{offset}, #{size}",
        "</script>"
    })
    List<OrderDetailVO> selectUserOrders(@Param("userId") Long userId,
                                       @Param("status") Integer status,
                                       @Param("offset") int offset,
                                       @Param("size") int size);

    /**
     * 查询场次订单列表
     * @param eventId 场次ID
     * @param status 订单状态（可选）
     * @param offset 偏移量
     * @param size 每页大小
     * @return 订单列表
     */
    @Select({
        "<script>",
        "SELECT o.*, oi.ticket_type_id, oi.quantity, oi.unit_price",
        "FROM t_order o",
        "LEFT JOIN t_order_item oi ON o.id = oi.order_id",
        "WHERE o.event_id = #{eventId}",
        "<if test='status != null'>",
        "  AND o.status = #{status}",
        "</if>",
        "AND o.deleted = 0",
        "ORDER BY o.create_time DESC",
        "LIMIT #{offset}, #{size}",
        "</script>"
    })
    List<OrderDetailVO> selectEventOrders(@Param("eventId") Long eventId,
                                        @Param("status") Integer status,
                                        @Param("offset") int offset,
                                        @Param("size") int size);

    /**
     * 统计用户在指定场次的订单数量
     * @param userId 用户ID
     * @param eventId 场次ID
     * @return 订单数量
     */
    @Select("SELECT COUNT(*) FROM t_order WHERE user_id = #{userId} AND event_id = #{eventId} AND deleted = 0")
    int countUserEventOrders(@Param("userId") Long userId, @Param("eventId") Long eventId);

    /**
     * 统计指定时间范围内的订单总数和金额
     */
    @Select({
        "SELECT COUNT(*) as orderCount,",
        "       COALESCE(SUM(total_amount), 0) as totalAmount",
        "FROM t_order",
        "WHERE create_time BETWEEN #{startTime} AND #{endTime}",
        "AND status IN (1, 3)",  // 已支付或已退款
        "AND deleted = 0"
    })
    Map<String, Object> selectOrderStatistics(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定场次的票种销售情况
     */
    @Select({
        "SELECT oi.ticket_type_id,",
        "       SUM(oi.quantity) as soldQuantity,",
        "       SUM(oi.amount) as totalAmount",
        "FROM t_order o",
        "JOIN t_order_item oi ON o.id = oi.order_id",
        "WHERE o.event_id = #{eventId}",
        "AND o.status = 1",  // 已支付
        "AND o.deleted = 0",
        "AND oi.deleted = 0",
        "GROUP BY oi.ticket_type_id"
    })
    List<Map<String, Object>> selectTicketTypeSalesStatistics(@Param("eventId") Long eventId);

    /**
     * 获取用户消费统计
     */
    @Select({
        "SELECT COUNT(*) as orderCount,",
        "       COALESCE(SUM(total_amount), 0) as totalAmount,",
        "       COALESCE(AVG(total_amount), 0) as avgAmount",
        "FROM t_order",
        "WHERE user_id = #{userId}",
        "AND status = 1",  // 已支付
        "AND deleted = 0"
    })
    Map<String, Object> selectUserOrderStatistics(@Param("userId") Long userId);

    /**
     * 获取热门票种排行
     */
    @Select({
        "SELECT oi.ticket_type_id,",
        "       SUM(oi.quantity) as soldQuantity,",
        "       SUM(oi.amount) as totalAmount",
        "FROM t_order o",
        "JOIN t_order_item oi ON o.id = oi.order_id",
        "WHERE o.create_time BETWEEN #{startTime} AND #{endTime}",
        "AND o.status = 1",  // 已支付
        "AND o.deleted = 0",
        "AND oi.deleted = 0",
        "GROUP BY oi.ticket_type_id",
        "ORDER BY soldQuantity DESC",
        "LIMIT #{limit}"
    })
    List<Map<String, Object>> selectHotTicketTypes(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime,
                                                  @Param("limit") int limit);

    /**
     * 获取每小时订单统计
     */
    @Select({
        "SELECT HOUR(create_time) as hour,",
        "       COUNT(*) as orderCount,",
        "       COALESCE(SUM(total_amount), 0) as totalAmount",
        "FROM t_order",
        "WHERE DATE(create_time) = DATE(#{date})",
        "AND status IN (1, 3)",  // 已支付或已退款
        "AND deleted = 0",
        "GROUP BY HOUR(create_time)",
        "ORDER BY hour"
    })
    List<Map<String, Object>> selectHourlyOrderStatistics(@Param("date") LocalDateTime date);

    /**
     * 获取退款率统计
     */
    @Select({
        "SELECT COUNT(*) as totalOrders,",
        "       SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as refundOrders,",
        "       COALESCE(SUM(CASE WHEN status = 3 THEN total_amount ELSE 0 END), 0) as refundAmount,",
        "       COALESCE(SUM(total_amount), 0) as totalAmount",
        "FROM t_order",
        "WHERE create_time BETWEEN #{startTime} AND #{endTime}",
        "AND status IN (1, 3)",  // 已支付或已退款
        "AND deleted = 0"
    })
    Map<String, Object> selectRefundStatistics(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);
} 