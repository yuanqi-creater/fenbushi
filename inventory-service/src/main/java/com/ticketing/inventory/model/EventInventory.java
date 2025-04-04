package com.ticketing.inventory.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 场次库存模型
 */
@Data
public class EventInventory {
    
    /**
     * 场次ID
     */
    private Long eventId;
    
    /**
     * 场次名称
     */
    private String eventName;
    
    /**
     * 场次时间
     */
    private LocalDateTime eventTime;
    
    /**
     * 场馆ID
     */
    private Long venueId;
    
    /**
     * 场馆名称
     */
    private String venueName;
    
    /**
     * 总座位数
     */
    private Integer totalSeats;
    
    /**
     * 剩余座位数
     */
    private Integer remainingSeats;
    
    /**
     * 票价类型列表
     */
    private List<TicketType> ticketTypes;
    
    /**
     * 销售状态
     * PENDING - 未开始
     * ON_SALE - 销售中
     * SOLD_OUT - 已售罄
     * CLOSED - 已结束
     */
    private String saleStatus;
    
    /**
     * 销售开始时间
     */
    private LocalDateTime saleStartTime;
    
    /**
     * 销售结束时间
     */
    private LocalDateTime saleEndTime;
    
    /**
     * 票价类型
     */
    @Data
    public static class TicketType {
        /**
         * 票价类型ID
         */
        private Long typeId;
        
        /**
         * 票价类型名称
         */
        private String typeName;
        
        /**
         * 票价（分）
         */
        private Integer price;
        
        /**
         * 总数量
         */
        private Integer totalQuantity;
        
        /**
         * 剩余数量
         */
        private Integer remainingQuantity;
        
        /**
         * 每人限购数量
         */
        private Integer limitPerPerson;
    }
} 