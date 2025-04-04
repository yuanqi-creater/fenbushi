-- 订单主表
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_number VARCHAR(32) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    event_id BIGINT NOT NULL COMMENT '场次ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已取消，3-已退款，4-已关闭',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    pay_time DATETIME COMMENT '支付时间',
    refund_time DATETIME COMMENT '退款时间',
    refund_reason VARCHAR(255) COMMENT '退款原因',
    expire_time DATETIME NOT NULL COMMENT '订单过期时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    UNIQUE KEY uk_order_number (order_number),
    KEY idx_user_id (user_id),
    KEY idx_event_id (event_id),
    KEY idx_create_time (create_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 订单项表
CREATE TABLE IF NOT EXISTS t_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    ticket_type_id BIGINT NOT NULL COMMENT '票种ID',
    quantity INT NOT NULL COMMENT '购买数量',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '单价',
    amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    KEY idx_order_id (order_id),
    KEY idx_ticket_type_id (ticket_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表'; 