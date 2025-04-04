package com.ticketing.order.service.impl;

import com.ticketing.common.entity.Order;
import com.ticketing.common.entity.OrderItem;
import com.ticketing.common.exception.BusinessException;
import com.ticketing.common.service.InventoryFeignClient;
import com.ticketing.common.service.TicketFeignClient;
import com.ticketing.common.service.UserFeignClient;
import com.ticketing.common.vo.CreateOrderVO;
import com.ticketing.common.vo.OrderDetailVO;
import com.ticketing.common.vo.PayOrderVO;
import com.ticketing.order.mapper.OrderItemMapper;
import com.ticketing.order.mapper.OrderMapper;
import com.ticketing.order.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务实现类
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "order")
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private InventoryFeignClient inventoryFeignClient;

    @Autowired
    private TicketFeignClient ticketFeignClient;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${order.timeout}")
    private Integer orderTimeout;

    @Value("${order.pay-timeout}")
    private Integer payTimeout;

    @Value("${order.number-prefix}")
    private String orderNumberPrefix;

    @Value("${order.max-orders-per-user-event}")
    private Integer maxOrdersPerUserEvent;

    // 订单状态常量
    public static final int STATUS_PENDING_PAYMENT = 0;    // 待支付
    public static final int STATUS_PAID = 1;              // 已支付
    public static final int STATUS_CANCELLED = 2;         // 已取消
    public static final int STATUS_REFUNDED = 3;          // 已退款
    public static final int STATUS_CLOSED = 4;            // 已关闭

    @Override
    @GlobalTransactional
    public Long createOrder(CreateOrderVO createOrderVO) {
        log.info("Creating order for user: {}, event: {}", createOrderVO.getUserId(), createOrderVO.getEventId());
        try {
            // 检查用户是否可以购买
            if (!checkUserCanBuy(createOrderVO.getUserId(), createOrderVO.getEventId())) {
                throw new BusinessException("超过该场次最大购买次数限制");
            }

            // 锁定库存
            boolean lockSuccess = inventoryFeignClient.lockStock(
                    createOrderVO.getEventId(),
                    createOrderVO.getTicketTypeId(),
                    createOrderVO.getUserId(),
                    createOrderVO.getQuantity()
            );
            if (!lockSuccess) {
                throw new BusinessException("库存不足");
            }

            // 创建订单
            Order order = new Order()
                    .setOrderNumber(generateOrderNumber())
                    .setUserId(createOrderVO.getUserId())
                    .setEventId(createOrderVO.getEventId())
                    .setStatus(STATUS_PENDING_PAYMENT)
                    .setTotalAmount(createOrderVO.getAmount())
                    .setCreateTime(LocalDateTime.now())
                    .setUpdateTime(LocalDateTime.now())
                    .setExpireTime(LocalDateTime.now().plusMinutes(payTimeout));

            orderMapper.insert(order);

            // 创建订单项
            OrderItem orderItem = new OrderItem()
                    .setOrderId(order.getId())
                    .setTicketTypeId(createOrderVO.getTicketTypeId())
                    .setQuantity(createOrderVO.getQuantity())
                    .setUnitPrice(createOrderVO.getUnitPrice())
                    .setAmount(createOrderVO.getAmount());

            orderItemMapper.insert(orderItem);

            // 发送延迟消息，用于关闭超时未支付的订单
            rocketMQTemplate.syncSend("order-timeout-check",
                    MessageBuilder.withPayload(order.getId()).build(),
                    3000,
                    payTimeout);

            log.info("Order created successfully: {}", order.getId());
            return order.getId();
        } catch (Exception e) {
            log.error("Failed to create order", e);
            throw new BusinessException("创建订单失败: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional
    @CacheEvict(key = "#payOrderVO.orderId")
    public boolean payOrder(PayOrderVO payOrderVO) {
        log.info("Processing payment for order: {}", payOrderVO.getOrderId());
        try {
            // 获取订单信息
            Order order = orderMapper.selectById(payOrderVO.getOrderId());
            if (order == null) {
                throw new BusinessException("订单不存在");
            }

            // 验证订单状态
            if (order.getStatus() != STATUS_PENDING_PAYMENT) {
                throw new BusinessException("订单状态不正确");
            }

            // 验证支付金额
            if (order.getTotalAmount().compareTo(payOrderVO.getAmount()) != 0) {
                throw new BusinessException("支付金额不正确");
            }

            // 验证订单是否过期
            if (LocalDateTime.now().isAfter(order.getExpireTime())) {
                throw new BusinessException("订单已过期");
            }

            // TODO: 调用支付服务进行实际支付
            // 这里模拟支付成功
            boolean paySuccess = true;

            if (paySuccess) {
                // 更新订单状态
                order.setStatus(STATUS_PAID)
                        .setPayTime(LocalDateTime.now())
                        .setUpdateTime(LocalDateTime.now());
                orderMapper.updateById(order);

                // 扣减库存
                OrderItem orderItem = orderItemMapper.selectByOrderId(order.getId());
                boolean deductSuccess = inventoryFeignClient.deductStock(
                        order.getEventId(),
                        orderItem.getTicketTypeId(),
                        order.getUserId(),
                        orderItem.getQuantity()
                );

                if (!deductSuccess) {
                    throw new BusinessException("扣减库存失败");
                }

                log.info("Order payment successful: {}", order.getId());
                return true;
            } else {
                throw new BusinessException("支付失败");
            }
        } catch (Exception e) {
            log.error("Failed to process payment for order: {}", payOrderVO.getOrderId(), e);
            throw new BusinessException("支付订单失败: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional
    @CacheEvict(key = "#orderId")
    public boolean cancelOrder(Long orderId, Long userId) {
        log.info("Cancelling order: {}", orderId);
        try {
            // 获取订单信息
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }

            // 验证用户权限
            if (!order.getUserId().equals(userId)) {
                throw new BusinessException("无权操作此订单");
            }

            // 验证订单状态
            if (order.getStatus() != STATUS_PENDING_PAYMENT) {
                throw new BusinessException("订单状态不正确");
            }

            // 更新订单状态
            order.setStatus(STATUS_CANCELLED)
                    .setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            // 释放库存
            OrderItem orderItem = orderItemMapper.selectByOrderId(orderId);
            boolean releaseSuccess = inventoryFeignClient.releaseStock(
                    order.getEventId(),
                    orderItem.getTicketTypeId(),
                    order.getUserId(),
                    orderItem.getQuantity()
            );

            if (!releaseSuccess) {
                throw new BusinessException("释放库存失败");
            }

            log.info("Order cancelled successfully: {}", orderId);
            return true;
        } catch (Exception e) {
            log.error("Failed to cancel order: {}", orderId, e);
            throw new BusinessException("取消订单失败: " + e.getMessage());
        }
    }

    @Override
    @Cacheable(key = "#orderId", unless = "#result == null")
    public OrderDetailVO getOrderDetail(Long orderId, Long userId) {
        log.info("Getting order detail: {}", orderId);
        try {
            // 获取订单信息
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }

            // 验证用户权限
            if (!order.getUserId().equals(userId)) {
                throw new BusinessException("无权查看此订单");
            }

            // 获取订单项信息
            OrderItem orderItem = orderItemMapper.selectByOrderId(orderId);

            // 构建订单详情
            return buildOrderDetailVO(order, orderItem);
        } catch (Exception e) {
            log.error("Failed to get order detail: {}", orderId, e);
            throw new BusinessException("获取订单详情失败: " + e.getMessage());
        }
    }

    @Override
    public List<OrderDetailVO> getUserOrders(Long userId, Integer status, int page, int size) {
        log.info("Getting user orders: userId={}, status={}, page={}, size={}", userId, status, page, size);
        try {
            return orderMapper.selectUserOrders(userId, status, (page - 1) * size, size);
        } catch (Exception e) {
            log.error("Failed to get user orders: {}", userId, e);
            throw new BusinessException("获取用户订单列表失败: " + e.getMessage());
        }
    }

    @Override
    public List<OrderDetailVO> getEventOrders(Long eventId, Integer status, int page, int size) {
        log.info("Getting event orders: eventId={}, status={}, page={}, size={}", eventId, status, page, size);
        try {
            return orderMapper.selectEventOrders(eventId, status, (page - 1) * size, size);
        } catch (Exception e) {
            log.error("Failed to get event orders: {}", eventId, e);
            throw new BusinessException("获取场次订单列表失败: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional
    @CacheEvict(key = "#orderId")
    public boolean closeTimeoutOrder(Long orderId) {
        log.info("Closing timeout order: {}", orderId);
        try {
            // 获取订单信息
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                return true;
            }

            // 检查订单状态
            if (order.getStatus() != STATUS_PENDING_PAYMENT) {
                return true;
            }

            // 检查是否已超时
            if (LocalDateTime.now().isBefore(order.getExpireTime())) {
                return true;
            }

            // 更新订单状态
            order.setStatus(STATUS_CLOSED)
                    .setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            // 释放库存
            OrderItem orderItem = orderItemMapper.selectByOrderId(orderId);
            boolean releaseSuccess = inventoryFeignClient.releaseStock(
                    order.getEventId(),
                    orderItem.getTicketTypeId(),
                    order.getUserId(),
                    orderItem.getQuantity()
            );

            if (!releaseSuccess) {
                throw new BusinessException("释放库存失败");
            }

            log.info("Order closed successfully: {}", orderId);
            return true;
        } catch (Exception e) {
            log.error("Failed to close order: {}", orderId, e);
            throw new BusinessException("关闭订单失败: " + e.getMessage());
        }
    }

    @Override
    @GlobalTransactional
    @CacheEvict(key = "#orderId")
    public boolean refundOrder(Long orderId, Long userId, String reason) {
        log.info("Processing refund for order: {}", orderId);
        try {
            // 获取订单信息
            Order order = orderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }

            // 验证用户权限
            if (!order.getUserId().equals(userId)) {
                throw new BusinessException("无权操作此订单");
            }

            // 验证订单状态
            if (order.getStatus() != STATUS_PAID) {
                throw new BusinessException("订单状态不正确");
            }

            // TODO: 调用支付服务进行退款
            // 这里模拟退款成功
            boolean refundSuccess = true;

            if (refundSuccess) {
                // 更新订单状态
                order.setStatus(STATUS_REFUNDED)
                        .setRefundTime(LocalDateTime.now())
                        .setRefundReason(reason)
                        .setUpdateTime(LocalDateTime.now());
                orderMapper.updateById(order);

                // 释放库存
                OrderItem orderItem = orderItemMapper.selectByOrderId(orderId);
                boolean releaseSuccess = inventoryFeignClient.releaseStock(
                        order.getEventId(),
                        orderItem.getTicketTypeId(),
                        order.getUserId(),
                        orderItem.getQuantity()
                );

                if (!releaseSuccess) {
                    throw new BusinessException("释放库存失败");
                }

                log.info("Order refunded successfully: {}", orderId);
                return true;
            } else {
                throw new BusinessException("退款失败");
            }
        } catch (Exception e) {
            log.error("Failed to refund order: {}", orderId, e);
            throw new BusinessException("退款失败: " + e.getMessage());
        }
    }

    @Override
    public boolean checkUserCanBuy(Long userId, Long eventId) {
        String key = "order:limit:" + eventId + ":" + userId;
        String countStr = redisTemplate.opsForValue().get(key);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);
        return count < maxOrdersPerUserEvent;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNumber() {
        return orderNumberPrefix +
                LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                String.format("%04d", (int) (Math.random() * 10000));
    }

    /**
     * 构建订单详情VO
     */
    private OrderDetailVO buildOrderDetailVO(Order order, OrderItem orderItem) {
        return new OrderDetailVO()
                .setId(order.getId())
                .setOrderNumber(order.getOrderNumber())
                .setUserId(order.getUserId())
                .setEventId(order.getEventId())
                .setTicketTypeId(orderItem.getTicketTypeId())
                .setQuantity(orderItem.getQuantity())
                .setUnitPrice(orderItem.getUnitPrice())
                .setTotalAmount(order.getTotalAmount())
                .setStatus(order.getStatus())
                .setCreateTime(order.getCreateTime())
                .setPayTime(order.getPayTime())
                .setRefundTime(order.getRefundTime())
                .setRefundReason(order.getRefundReason())
                .setExpireTime(order.getExpireTime());
    }
} 