package com.ticketing.order.service;

import com.ticketing.order.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * 取消订单
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 支付订单
     */
    PaymentResponse payOrder(Long orderId, String paymentMethod);

    /**
     * 申请退票
     */
    RefundResponse refundOrder(RefundRequest request);

    /**
     * 获取订单详情
     */
    OrderDetail getOrderDetail(Long orderId, Long userId);

    /**
     * 获取用户订单列表
     */
    Page<OrderSummary> getUserOrders(Long userId, OrderStatus status, Pageable pageable);

    /**
     * 获取活动订单列表
     */
    Page<OrderSummary> getEventOrders(Long eventId, OrderStatus status, Pageable pageable);

    /**
     * 确认出票
     */
    void confirmTicketIssue(Long orderId);

    /**
     * 获取电子票
     */
    List<ETicket> getETickets(Long orderId, Long userId);

    /**
     * 更新订单状态
     */
    void updateOrderStatus(Long orderId, OrderStatus status, String reason);

    /**
     * 获取订单统计
     */
    OrderStatistics getOrderStatistics(Long userId);

    /**
     * 导出订单
     */
    byte[] exportOrders(OrderExportRequest request);

    /**
     * 批量取消超时未支付订单
     */
    void cancelTimeoutOrders();

    /**
     * 订单状态枚举
     */
    enum OrderStatus {
        PENDING_PAYMENT,    // 待支付
        PAID,              // 已支付
        TICKET_ISSUED,     // 已出票
        COMPLETED,         // 已完成
        CANCELLED,         // 已取消
        REFUNDING,         // 退款中
        REFUNDED          // 已退款
    }

    /**
     * 内部类：订单统计
     */
    class OrderStatistics {
        private int totalOrders;
        private int pendingPayment;
        private int paid;
        private int completed;
        private int cancelled;
        private int refunded;
        // getter and setter
    }
} 