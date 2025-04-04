package com.ticketing.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 库存实体类
 */
@Data
@Accessors(chain = true)
@TableName("t_inventory")
public class Inventory {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 场次ID
     */
    private Long eventId;
    
    /**
     * 票种ID
     */
    private Long ticketTypeId;
    
    /**
     * 分片ID（用于分段锁）
     */
    private Integer shardingId;
    
    /**
     * 总库存
     */
    private Integer totalStock;
    
    /**
     * 已售库存
     */
    private Integer soldStock;
    
    /**
     * 锁定库存（已下单未支付）
     */
    private Integer lockedStock;
    
    /**
     * 可用库存
     */
    private Integer availableStock;
    
    /**
     * 版本号（乐观锁）
     */
    private Integer version;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 