package com.ticketing.payment.service;

import com.ticketing.payment.model.PaymentRequest;
import com.ticketing.payment.model.PaymentResponse;
import com.ticketing.payment.model.RefundRequest;
import com.ticketing.payment.model.RefundResponse;

import java.math.BigDecimal;

public interface PaymentService {
    /**
     * 创建支付订单
     */
    PaymentResponse createPayment(PaymentRequest request);

    /**
     * 处理支付回调
     */
    void handlePaymentCallback(String paymentId, String tradeNo, String status);

    /**
     * 查询支付状态
     */
    String queryPaymentStatus(String paymentId);

    /**
     * 申请退款
     */
    RefundResponse refund(RefundRequest request);

    /**
     * 处理退款回调
     */
    void handleRefundCallback(String refundId, String status);

    /**
     * 查询退款状态
     */
    String queryRefundStatus(String refundId);

    /**
     * 获取支付二维码（微信/支付宝）
     */
    String getPaymentQRCode(String paymentId);

    /**
     * 关闭支付订单
     */
    void closePayment(String paymentId);

    /**
     * 获取支付统计
     */
    PaymentStatistics getPaymentStatistics(Long userId);

    /**
     * 内部类：支付统计
     */
    class PaymentStatistics {
        private BigDecimal totalAmount;
        private int totalCount;
        private BigDecimal refundAmount;
        private int refundCount;
        // getter and setter
    }
} 