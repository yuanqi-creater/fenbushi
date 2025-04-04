package com.ticketing.inventory.monitor;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * 库存监控指标类
 */
@Slf4j
@Component
public class InventoryMetrics {

    @Autowired
    private MeterRegistry meterRegistry;

    // 库存操作计数器
    private Counter deductSuccessCounter;
    private Counter deductFailCounter;
    private Counter lockSuccessCounter;
    private Counter lockFailCounter;

    // 库存操作延迟统计
    private Timer deductLatencyTimer;
    private Timer lockLatencyTimer;

    // 库存余量监控
    private Gauge inventoryGauge;

    // 消息队列监控
    private Gauge messageQueueSizeGauge;
    private Counter messageProcessedCounter;

    @PostConstruct
    public void init() {
        // 初始化计数器
        deductSuccessCounter = Counter.builder("inventory_deduct_total")
                .tag("result", "success")
                .description("Total number of successful inventory deductions")
                .register(meterRegistry);

        deductFailCounter = Counter.builder("inventory_deduct_total")
                .tag("result", "fail")
                .description("Total number of failed inventory deductions")
                .register(meterRegistry);

        lockSuccessCounter = Counter.builder("inventory_lock_total")
                .tag("result", "success")
                .description("Total number of successful inventory locks")
                .register(meterRegistry);

        lockFailCounter = Counter.builder("inventory_lock_total")
                .tag("result", "fail")
                .description("Total number of failed inventory locks")
                .register(meterRegistry);

        // 初始化延迟统计
        deductLatencyTimer = Timer.builder("inventory_deduct_latency")
                .description("Inventory deduction latency")
                .register(meterRegistry);

        lockLatencyTimer = Timer.builder("inventory_lock_latency")
                .description("Inventory lock latency")
                .register(meterRegistry);

        // 初始化库存监控
        inventoryGauge = Gauge.builder("inventory_remaining", this, InventoryMetrics::getInventoryRemaining)
                .description("Remaining inventory quantity")
                .register(meterRegistry);

        // 初始化消息队列监控
        messageQueueSizeGauge = Gauge.builder("message_queue_size", this, InventoryMetrics::getMessageQueueSize)
                .description("Current message queue size")
                .register(meterRegistry);

        messageProcessedCounter = Counter.builder("message_processed_total")
                .description("Total number of processed messages")
                .register(meterRegistry);
    }

    // 记录库存扣减成功
    public void recordDeductSuccess(long startTime) {
        deductSuccessCounter.increment();
        recordLatency(deductLatencyTimer, startTime);
    }

    // 记录库存扣减失败
    public void recordDeductFail(long startTime) {
        deductFailCounter.increment();
        recordLatency(deductLatencyTimer, startTime);
    }

    // 记录库存锁定成功
    public void recordLockSuccess(long startTime) {
        lockSuccessCounter.increment();
        recordLatency(lockLatencyTimer, startTime);
    }

    // 记录库存锁定失败
    public void recordLockFail(long startTime) {
        lockFailCounter.increment();
        recordLatency(lockLatencyTimer, startTime);
    }

    // 记录消息处理
    public void recordMessageProcessed() {
        messageProcessedCounter.increment();
    }

    // 记录操作延迟
    private void recordLatency(Timer timer, long startTime) {
        timer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);
    }

    // 获取当前库存余量（示例方法）
    private double getInventoryRemaining() {
        // 实际实现需要从库存服务获取
        return 0.0;
    }

    // 获取当前消息队列大小（示例方法）
    private double getMessageQueueSize() {
        // 实际实现需要从消息队列获取
        return 0.0;
    }
} 