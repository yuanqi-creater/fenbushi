package com.ticketing.order.service;

import com.ticketing.common.entity.Order;
import com.ticketing.common.entity.OrderItem;
import com.ticketing.common.exception.BusinessException;
import com.ticketing.common.service.InventoryFeignClient;
import com.ticketing.common.vo.CreateOrderVO;
import com.ticketing.common.vo.OrderDetailVO;
import com.ticketing.common.vo.PayOrderVO;
import com.ticketing.order.mapper.OrderItemMapper;
import com.ticketing.order.mapper.OrderMapper;
import com.ticketing.order.service.impl.OrderServiceImpl;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private InventoryFeignClient inventoryFeignClient;

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void createOrder_Success() {
        // 准备测试数据
        CreateOrderVO createOrderVO = new CreateOrderVO()
                .setUserId(1L)
                .setEventId(1L)
                .setTicketTypeId(1L)
                .setQuantity(2)
                .setUnitPrice(new BigDecimal("100"))
                .setAmount(new BigDecimal("200"));

        // Mock外部依赖
        when(inventoryFeignClient.lockStock(anyLong(), anyLong(), anyLong(), anyInt())).thenReturn(true);
        when(orderMapper.insert(any())).thenReturn(1);
        when(orderItemMapper.insert(any())).thenReturn(1);
        when(valueOperations.get(anyString())).thenReturn("0");

        // 执行测试
        Long orderId = orderService.createOrder(createOrderVO);

        // 验证结果
        assertNotNull(orderId);
        verify(orderMapper).insert(any());
        verify(orderItemMapper).insert(any());
        verify(rocketMQTemplate).syncSend(eq("order-timeout-check"), any(), anyLong(), anyInt());
    }

    @Test
    void createOrder_InventoryLockFailed() {
        // 准备测试数据
        CreateOrderVO createOrderVO = new CreateOrderVO()
                .setUserId(1L)
                .setEventId(1L)
                .setTicketTypeId(1L)
                .setQuantity(2)
                .setUnitPrice(new BigDecimal("100"))
                .setAmount(new BigDecimal("200"));

        // Mock外部依赖
        when(inventoryFeignClient.lockStock(anyLong(), anyLong(), anyLong(), anyInt())).thenReturn(false);
        when(valueOperations.get(anyString())).thenReturn("0");

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> orderService.createOrder(createOrderVO));
    }

    @Test
    void payOrder_Success() {
        // 准备测试数据
        PayOrderVO payOrderVO = new PayOrderVO()
                .setOrderId(1L)
                .setAmount(new BigDecimal("200"));

        Order order = new Order()
                .setId(1L)
                .setStatus(OrderServiceImpl.STATUS_PENDING_PAYMENT)
                .setTotalAmount(new BigDecimal("200"))
                .setExpireTime(LocalDateTime.now().plusMinutes(30));

        OrderItem orderItem = new OrderItem()
                .setOrderId(1L)
                .setTicketTypeId(1L)
                .setQuantity(2);

        // Mock外部依赖
        when(orderMapper.selectById(anyLong())).thenReturn(order);
        when(orderItemMapper.selectByOrderId(anyLong())).thenReturn(orderItem);
        when(inventoryFeignClient.deductStock(anyLong(), anyLong(), anyLong(), anyInt())).thenReturn(true);
        when(orderMapper.updateById(any())).thenReturn(1);

        // 执行测试
        boolean result = orderService.payOrder(payOrderVO);

        // 验证结果
        assertTrue(result);
        verify(orderMapper).updateById(any());
        verify(inventoryFeignClient).deductStock(anyLong(), anyLong(), anyLong(), anyInt());
    }

    @Test
    void getUserOrders_Success() {
        // 准备测试数据
        OrderDetailVO order1 = new OrderDetailVO().setId(1L);
        OrderDetailVO order2 = new OrderDetailVO().setId(2L);
        List<OrderDetailVO> expectedOrders = Arrays.asList(order1, order2);

        // Mock外部依赖
        when(orderMapper.selectUserOrders(anyLong(), any(), anyInt(), anyInt())).thenReturn(expectedOrders);

        // 执行测试
        List<OrderDetailVO> actualOrders = orderService.getUserOrders(1L, null, 1, 10);

        // 验证结果
        assertEquals(expectedOrders.size(), actualOrders.size());
        assertEquals(expectedOrders.get(0).getId(), actualOrders.get(0).getId());
        assertEquals(expectedOrders.get(1).getId(), actualOrders.get(1).getId());
    }

    @Test
    void cancelOrder_Success() {
        // 准备测试数据
        Order order = new Order()
                .setId(1L)
                .setUserId(1L)
                .setStatus(OrderServiceImpl.STATUS_PENDING_PAYMENT);

        OrderItem orderItem = new OrderItem()
                .setOrderId(1L)
                .setTicketTypeId(1L)
                .setQuantity(2);

        // Mock外部依赖
        when(orderMapper.selectById(anyLong())).thenReturn(order);
        when(orderItemMapper.selectByOrderId(anyLong())).thenReturn(orderItem);
        when(inventoryFeignClient.releaseStock(anyLong(), anyLong(), anyLong(), anyInt())).thenReturn(true);
        when(orderMapper.updateById(any())).thenReturn(1);

        // 执行测试
        boolean result = orderService.cancelOrder(1L, 1L);

        // 验证结果
        assertTrue(result);
        verify(orderMapper).updateById(any());
        verify(inventoryFeignClient).releaseStock(anyLong(), anyLong(), anyLong(), anyInt());
    }
} 