package com.ticketing.inventory.monitor;

import com.ticketing.inventory.service.InventoryShardingService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 库存监控服务
 * 监控库存操作性能和准确性
 */
@Slf4j
@Service
public class InventoryMonitorService {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private InventoryShardingService inventoryShardingService;

    // 性能指标
    private Timer lockStockTimer;
    private Timer deductStockTimer;
    private Timer releaseStockTimer;

    // 操作计数器
    private Map<String, OperationCounter> operationCounters = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // 初始化性能指标
        lockStockTimer = Timer.builder("inventory_lock_stock_timer")
                .description("库存锁定操作耗时")
                .register(meterRegistry);

        deductStockTimer = Timer.builder("inventory_deduct_stock_timer")
                .description("库存扣减操作耗时")
                .register(meterRegistry);

        releaseStockTimer = Timer.builder("inventory_release_stock_timer")
                .description("库存释放操作耗时")
                .register(meterRegistry);

        // 注册库存操作成功率指标
        meterRegistry.gauge("inventory_operation_success_rate", operationCounters,
                counters -> calculateSuccessRate());
    }

    /**
     * 记录库存锁定操作
     */
    public void recordLockStock(Long eventId, Long ticketTypeId, boolean success, long startTime) {
        // 记录耗时
        lockStockTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);

        // 更新操作计数
        String key = getOperationKey(eventId, ticketTypeId, "lock");
        operationCounters.computeIfAbsent(key, k -> new OperationCounter())
                .recordOperation(success);

        // 记录日志
        if (!success) {
            log.warn("Lock stock failed for event: {}, ticketType: {}", eventId, ticketTypeId);
        }
    }

    /**
     * 记录库存扣减操作
     */
    public void recordDeductStock(Long eventId, Long ticketTypeId, boolean success, long startTime) {
        // 记录耗时
        deductStockTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);

        // 更新操作计数
        String key = getOperationKey(eventId, ticketTypeId, "deduct");
        operationCounters.computeIfAbsent(key, k -> new OperationCounter())
                .recordOperation(success);

        // 记录日志
        if (!success) {
            log.warn("Deduct stock failed for event: {}, ticketType: {}", eventId, ticketTypeId);
        }
    }

    /**
     * 记录库存释放操作
     */
    public void recordReleaseStock(Long eventId, Long ticketTypeId, boolean success, long startTime) {
        // 记录耗时
        releaseStockTimer.record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);

        // 更新操作计数
        String key = getOperationKey(eventId, ticketTypeId, "release");
        operationCounters.computeIfAbsent(key, k -> new OperationCounter())
                .recordOperation(success);

        // 记录日志
        if (!success) {
            log.warn("Release stock failed for event: {}, ticketType: {}", eventId, ticketTypeId);
        }
    }

    /**
     * 定时检查库存操作性能
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void checkPerformance() {
        // 检查库存锁定性能
        double p99LockTime = lockStockTimer.percentile(0.99);
        if (p99LockTime > 80) { // TP99 > 80ms
            log.warn("Lock stock performance degraded, P99: {}ms", p99LockTime);
        }

        // 检查库存扣减性能
        double p99DeductTime = deductStockTimer.percentile(0.99);
        if (p99DeductTime > 80) {
            log.warn("Deduct stock performance degraded, P99: {}ms", p99DeductTime);
        }

        // 检查库存释放性能
        double p99ReleaseTime = releaseStockTimer.percentile(0.99);
        if (p99ReleaseTime > 80) {
            log.warn("Release stock performance degraded, P99: {}ms", p99ReleaseTime);
        }
    }

    /**
     * 定时检查库存操作准确性
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void checkAccuracy() {
        double successRate = calculateSuccessRate();
        if (successRate < 0.9999) { // 准确率低于99.99%
            log.error("Inventory operation accuracy is too low: {}%", successRate * 100);
        }
    }

    /**
     * 计算操作成功率
     */
    private double calculateSuccessRate() {
        long totalSuccess = 0;
        long totalOperations = 0;

        for (OperationCounter counter : operationCounters.values()) {
            totalSuccess += counter.getSuccessCount();
            totalOperations += counter.getTotalCount();
        }

        return totalOperations == 0 ? 1.0 : (double) totalSuccess / totalOperations;
    }

    /**
     * 生成操作key
     */
    private String getOperationKey(Long eventId, Long ticketTypeId, String operation) {
        return String.format("%d:%d:%s", eventId, ticketTypeId, operation);
    }

    /**
     * 操作计数器
     */
    @lombok.Data
    private static class OperationCounter {
        private long successCount;
        private long totalCount;

        public synchronized void recordOperation(boolean success) {
            if (success) {
                successCount++;
            }
            totalCount++;
        }
    }
} 