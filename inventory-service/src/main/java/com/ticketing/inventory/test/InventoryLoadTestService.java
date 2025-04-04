package com.ticketing.inventory.test;

import com.ticketing.inventory.async.AsyncInventoryService;
import com.ticketing.inventory.hotspot.HotspotDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 库存系统压力测试服务
 * 使用影子表进行全链路压测
 */
@Slf4j
@Service
public class InventoryLoadTestService {

    @Autowired
    private AsyncInventoryService asyncInventoryService;

    @Autowired
    private HotspotDetectionService hotspotDetectionService;

    // 线程池配置
    private static final int CORE_POOL_SIZE = 100;
    private static final int MAX_POOL_SIZE = 200;
    private static final int QUEUE_CAPACITY = 10000;
    
    // 压测配置
    private static final int TARGET_QPS = 42000;
    private static final int TEST_DURATION_SECONDS = 180; // 3分钟
    private static final int TOTAL_TICKETS = 500000;
    
    // 统计数据
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger failCount = new AtomicInteger(0);
    private List<Long> responseTimes = new CopyOnWriteArrayList<>();

    /**
     * 执行压力测试
     */
    public TestResult runLoadTest(Long eventId, Long ticketTypeId) {
        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // 计算每秒需要提交的请求数
        int requestsPerSecond = TARGET_QPS;
        long intervalMillis = 1000 / requestsPerSecond;

        log.info("Starting load test: target QPS={}, duration={}s", TARGET_QPS, TEST_DURATION_SECONDS);

        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 提交压测请求
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() - startTime < TEST_DURATION_SECONDS * 1000) {
                submitTestRequest(executor, eventId, ticketTypeId);
            } else {
                scheduler.shutdown();
            }
        }, 0, intervalMillis, TimeUnit.MILLISECONDS);

        // 等待测试完成
        try {
            scheduler.awaitTermination(TEST_DURATION_SECONDS + 5, TimeUnit.SECONDS);
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Load test interrupted", e);
        }

        // 计算测试结果
        return calculateTestResult(startTime);
    }

    /**
     * 提交测试请求
     */
    private void submitTestRequest(ThreadPoolExecutor executor, Long eventId, Long ticketTypeId) {
        executor.submit(() -> {
            long startTime = System.currentTimeMillis();
            try {
                // 随机生成用户ID和购票数量
                Long userId = ThreadLocalRandom.current().nextLong(1, 100000);
                int quantity = ThreadLocalRandom.current().nextInt(1, 5);

                // 发送库存扣减请求
                asyncInventoryService.sendDeductMessage(eventId, ticketTypeId, userId, quantity);
                
                // 记录响应时间
                long responseTime = System.currentTimeMillis() - startTime;
                responseTimes.add(responseTime);
                
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
                log.error("Test request failed", e);
            }
        });
    }

    /**
     * 计算测试结果
     */
    private TestResult calculateTestResult(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        int totalRequests = successCount.get() + failCount.get();
        double actualQps = totalRequests * 1000.0 / duration;
        
        // 计算响应时间统计
        long[] sortedTimes = responseTimes.stream().mapToLong(Long::valueOf).sorted().toArray();
        long avgResponseTime = (long) responseTimes.stream().mapToLong(Long::valueOf).average().orElse(0);
        long p95ResponseTime = sortedTimes[(int)(sortedTimes.length * 0.95)];
        long p99ResponseTime = sortedTimes[(int)(sortedTimes.length * 0.99)];

        return new TestResult(
                totalRequests,
                successCount.get(),
                failCount.get(),
                actualQps,
                avgResponseTime,
                p95ResponseTime,
                p99ResponseTime
        );
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class TestResult {
        private int totalRequests;
        private int successCount;
        private int failCount;
        private double actualQps;
        private long avgResponseTime;
        private long p95ResponseTime;
        private long p99ResponseTime;
    }
} 