package com.ticketing.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 票种实体类
 */
@Data
@Accessors(chain = true)
@TableName("t_ticket_type")
public class TicketType {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 场次ID
     */
    private Long eventId;
    
    /**
     * 票种名称
     */
    private String name;
    
    /**
     * 票种描述
     */
    private String description;
    
    /**
     * 票价
     */
    private BigDecimal price;
    
    /**
     * 限购数量
     */
    private Integer limitPerOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 