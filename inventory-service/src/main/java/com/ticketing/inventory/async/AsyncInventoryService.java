package com.ticketing.inventory.async;

import com.ticketing.inventory.service.InventoryShardingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 异步库存扣减服务
 * 基于RocketMQ实现最终一致性的库存扣减
 */
@Slf4j
@Service
public class AsyncInventoryService {

    @Autowired
    private InventoryShardingService inventoryShardingService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    // 批量处理缓冲区
    private Map<String, List<DeductRequest>> batchBuffer = new ConcurrentHashMap<>();
    
    // 批处理大小
    private static final int BATCH_SIZE = 100;
    
    // 批处理间隔（毫秒）
    private static final long BATCH_INTERVAL = 100;

    // 批处理线程池
    private ScheduledExecutorService executorService;

    public AsyncInventoryService() {
        executorService = new ScheduledThreadPoolExecutor(1);
        // 启动定时批处理任务
        executorService.scheduleAtFixedRate(
                this::processBatch,
                BATCH_INTERVAL,
                BATCH_INTERVAL,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * 发送库存扣减消息
     */
    public void sendDeductMessage(Long eventId, Long ticketTypeId, Long userId, int quantity) {
        DeductMessage message = new DeductMessage(eventId, ticketTypeId, userId, quantity);
        rocketMQTemplate.syncSend("inventory-deduct-topic", message);
        log.info("Sent deduct message: {}", message);
    }

    /**
     * 库存扣减消息监听器
     */
    @Service
    @RocketMQMessageListener(
            topic = "inventory-deduct-topic",
            consumerGroup = "inventory-deduct-group"
    )
    public class DeductMessageListener implements RocketMQListener<DeductMessage> {
        
        @Override
        public void onMessage(DeductMessage message) {
            try {
                // 添加到批处理缓冲区
                String key = getBufferKey(message.getEventId(), message.getTicketTypeId());
                batchBuffer.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new DeductRequest(
                                message.getEventId(),
                                message.getTicketTypeId(),
                                message.getUserId(),
                                message.getQuantity()
                        ));

                log.info("Added to batch buffer: {}", message);
            } catch (Exception e) {
                log.error("Failed to process deduct message", e);
            }
        }
    }

    /**
     * 执行批量处理
     */
    private void processBatch() {
        for (Map.Entry<String, List<DeductRequest>> entry : batchBuffer.entrySet()) {
            List<DeductRequest> requests = entry.getValue();
            
            if (requests.size() >= BATCH_SIZE) {
                try {
                    // 批量扣减库存
                    processBatchDeduct(requests);
                    
                    // 清空已处理的请求
                    requests.clear();
                } catch (Exception e) {
                    log.error("Failed to process batch deduct", e);
                }
            }
        }
    }

    /**
     * 处理批量扣减请求
     */
    private void processBatchDeduct(List<DeductRequest> requests) {
        if (requests.isEmpty()) {
            return;
        }

        // 按事件和票种分组
        Map<String, Integer> totalDeducts = new ConcurrentHashMap<>();
        
        for (DeductRequest request : requests) {
            String key = getBufferKey(request.getEventId(), request.getTicketTypeId());
            totalDeducts.merge(key, request.getQuantity(), Integer::sum);
        }

        // 批量扣减
        for (Map.Entry<String, Integer> entry : totalDeducts.entrySet()) {
            String[] keys = entry.getKey().split(":");
            Long eventId = Long.parseLong(keys[0]);
            Long ticketTypeId = Long.parseLong(keys[1]);
            
            try {
                // 执行实际的库存扣减
                boolean success = inventoryShardingService.deductStock(
                        eventId,
                        ticketTypeId,
                        null, // 批量处理时不需要用户ID
                        entry.getValue()
                );

                if (success) {
                    log.info("Batch deduct success: eventId={}, ticketTypeId={}, quantity={}",
                            eventId, ticketTypeId, entry.getValue());
                } else {
                    log.error("Batch deduct failed: eventId={}, ticketTypeId={}, quantity={}",
                            eventId, ticketTypeId, entry.getValue());
                }
            } catch (Exception e) {
                log.error("Failed to deduct stock", e);
            }
        }
    }

    /**
     * 生成缓冲区key
     */
    private String getBufferKey(Long eventId, Long ticketTypeId) {
        return eventId + ":" + ticketTypeId;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class DeductMessage {
        private Long eventId;
        private Long ticketTypeId;
        private Long userId;
        private int quantity;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class DeductRequest {
        private Long eventId;
        private Long ticketTypeId;
        private Long userId;
        private int quantity;
    }
} 