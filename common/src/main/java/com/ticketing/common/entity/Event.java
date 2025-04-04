package com.ticketing.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 场次实体类
 */
@Data
@Accessors(chain = true)
@TableName("t_event")
public class Event {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 活动名称
     */
    private String name;
    
    /**
     * 活动描述
     */
    private String description;
    
    /**
     * 活动地点
     */
    private String venue;
    
    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 售票开始时间
     */
    private LocalDateTime saleStartTime;
    
    /**
     * 售票结束时间
     */
    private LocalDateTime saleEndTime;
    
    /**
     * 活动状态（0-未开始，1-进行中，2-已结束）
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 