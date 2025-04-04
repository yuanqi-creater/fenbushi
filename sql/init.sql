-- 创建数据库
CREATE DATABASE IF NOT EXISTS ticketing DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ticketing;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(100) NOT NULL COMMENT '密码',
    `phone` varchar(20) NOT NULL COMMENT '手机号',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
    `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 活动表
CREATE TABLE IF NOT EXISTS `event` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `name` varchar(100) NOT NULL COMMENT '活动名称',
    `description` text COMMENT '活动描述',
    `venue` varchar(100) NOT NULL COMMENT '活动场地',
    `event_time` datetime NOT NULL COMMENT '活动时间',
    `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0-未开始，1-进行中，2-已结束',
    `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- 票品表
CREATE TABLE IF NOT EXISTS `ticket` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '票品ID',
    `event_id` bigint(20) NOT NULL COMMENT '活动ID',
    `name` varchar(50) NOT NULL COMMENT '票品名称',
    `price` decimal(10,2) NOT NULL COMMENT '票价',
    `total_quantity` int(11) NOT NULL COMMENT '总数量',
    `available_quantity` int(11) NOT NULL COMMENT '可用数量',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-下架，1-上架',
    `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='票品表';

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` varchar(32) NOT NULL COMMENT '订单编号',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `event_id` bigint(20) NOT NULL COMMENT '活动ID',
    `ticket_id` bigint(20) NOT NULL COMMENT '票品ID',
    `quantity` int(11) NOT NULL COMMENT '购买数量',
    `amount` decimal(10,2) NOT NULL COMMENT '订单金额',
    `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0-待支付，1-已支付，2-已取消，3-已退款',
    `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_event_id` (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 支付表
CREATE TABLE IF NOT EXISTS `payment` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '支付ID',
    `payment_no` varchar(32) NOT NULL COMMENT '支付流水号',
    `order_no` varchar(32) NOT NULL COMMENT '订单编号',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
    `payment_method` varchar(20) NOT NULL COMMENT '支付方式：ALIPAY-支付宝，WECHAT-微信',
    `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0-待支付，1-支付成功，2-支付失败',
    `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_payment_no` (`payment_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付表';

-- 退款表
CREATE TABLE IF NOT EXISTS `refund` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '退款ID',
    `refund_no` varchar(32) NOT NULL COMMENT '退款流水号',
    `order_no` varchar(32) NOT NULL COMMENT '订单编号',
    `payment_no` varchar(32) NOT NULL COMMENT '支付流水号',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `amount` decimal(10,2) NOT NULL COMMENT '退款金额',
    `reason` varchar(200) DEFAULT NULL COMMENT '退款原因',
    `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态：0-待处理，1-退款成功，2-退款失败',
    `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_payment_no` (`payment_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款表';

-- 库存锁定表
CREATE TABLE IF NOT EXISTS `inventory_lock` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '锁定ID',
    `ticket_id` bigint(20) NOT NULL COMMENT '票品ID',
    `order_no` varchar(32) NOT NULL COMMENT '订单编号',
    `quantity` int(11) NOT NULL COMMENT '锁定数量',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：0-已释放，1-锁定中',
    `expire_time` datetime NOT NULL COMMENT '过期时间',
    `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ticket_order` (`ticket_id`,`order_no`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存锁定表';

-- 插入测试数据
INSERT INTO `user` (`username`, `password`, `phone`, `email`) VALUES
('admin', '$2a$10$X/hX6J2sUOq9xj7TJF4XOeY2PF1.9E6XA3X6X2X9X2X9X2X9X2', '13800138000', 'admin@example.com');

INSERT INTO `event` (`name`, `description`, `venue`, `event_time`) VALUES
('Taylor Swift Eras Tour', 'Taylor Swift Eras Tour in Shanghai', 'Mercedes-Benz Arena', '2024-06-15 19:30:00');

INSERT INTO `ticket` (`event_id`, `name`, `price`, `total_quantity`, `available_quantity`) VALUES
(1, 'VIP票', 2888.00, 1000, 1000),
(1, '普通票', 1888.00, 5000, 5000); 