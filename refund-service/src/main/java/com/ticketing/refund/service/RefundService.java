package com.ticketing.refund.service;

import com.ticketing.refund.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RefundService {
    /**
     * 申请退票
     */
    RefundResponse applyRefund(RefundRequest request);

    /**
     * 审核退票申请
     */
    void reviewRefund(Long refundId, boolean approved, String reason);

    /**
     * 处理退款
     */
    void processRefund(Long refundId);

    /**
     * 获取退票详情
     */
    RefundDetail getRefundDetail(Long refundId);

    /**
     * 获取用户退票记录
     */
    Page<RefundRecord> getUserRefunds(Long userId, RefundStatus status, Pageable pageable);

    /**
     * 获取活动退票统计
     */
    RefundStatistics getEventRefundStatistics(Long eventId);

    /**
     * 计算退票费用
     */
    RefundFeeResponse calculateRefundFee(RefundFeeRequest request);

    /**
     * 检查退票资格
     */
    RefundEligibility checkRefundEligibility(Long orderId, Long userId);

    /**
     * 取消退票申请
     */
    void cancelRefund(Long refundId, Long userId);

    /**
     * 退票状态枚举
     */
    enum RefundStatus {
        PENDING_REVIEW,    // 待审核
        APPROVED,          // 已批准
        REJECTED,          // 已拒绝
        PROCESSING,        // 处理中
        COMPLETED,         // 已完成
        CANCELLED         // 已取消
    }

    /**
     * 内部类：退票统计
     */
    class RefundStatistics {
        private int totalRefunds;
        private int approvedRefunds;
        private int rejectedRefunds;
        private double refundRate;
        private double averageRefundTime;
        // getter and setter
    }

    /**
     * 内部类：退票资格
     */
    class RefundEligibility {
        private boolean eligible;
        private String reason;
        private double refundableAmount;
        private double penaltyFee;
        private long deadlineTimestamp;
        // getter and setter
    }
} 