# 分布式票务系统

这是一个基于Spring Cloud Alibaba的分布式票务系统，用于处理大规模的在线售票场景。

## 项目结构

```
fenbushi/
├── auth-service/        # 认证服务：处理用户认证和授权
├── inventory-service/   # 库存服务：管理票务库存和库存锁定
├── order-service/      # 订单服务：处理订单创建和管理
├── payment-service/    # 支付服务：处理支付和退款
├── user-service/       # 用户服务：用户管理和信息维护
├── common/            # 公共模块：共享的工具类和配置
├── ticketing-web/     # 前端项目：React + Ant Design
├── sql/              # 数据库脚本
└── docs/             # 项目文档
```

## 技术栈

### 后端
- Spring Cloud Alibaba
- Spring Boot 2.6.3
- MySQL 8.0
- Redis
- RocketMQ
- Nacos
- Seata
- MyBatis-Plus

### 前端
- React 17
- Ant Design 4
- React Router 6
- Axios

## 核心功能

1. 用户管理
   - 注册登录
   - 个人中心
   - 密码修改

2. 票务管理
   - 活动列表
   - 票品管理
   - 库存控制

3. 订单系统
   - 订单创建
   - 订单支付
   - 订单查询

4. 支付系统
   - 在线支付
   - 退款处理
   - 支付记录

## 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Node.js 14+

## 快速开始

1. 克隆项目
```bash
git clone [项目地址]
cd fenbushi
```

2. 初始化数据库
```bash
# 执行SQL脚本
mysql -u root -p < sql/init.sql
```

3. 启动后端服务
```bash
# 编译
mvn clean install

# 启动服务（按顺序）
cd auth-service
mvn spring-boot:run

cd ../inventory-service
mvn spring-boot:run

cd ../order-service
mvn spring-boot:run

cd ../payment-service
mvn spring-boot:run

cd ../user-service
mvn spring-boot:run
```

4. 启动前端项目
```bash
cd ticketing-web
npm install
npm start
```

## 开发指南

详细的开发指南请参考 `docs` 目录下的文档：

- [开发环境搭建](docs/setup.md)
- [项目配置说明](docs/configuration.md)
- [API文档](docs/api.md)
- [部署指南](docs/deployment.md)

## 性能特性

- 高并发抢票支持
- 分布式事务保证
- 防超卖机制
- 秒杀性能优化

## 贡献指南

1. Fork 本仓库
2. 创建新的分支 `git checkout -b feature/your-feature`
3. 提交更改 `git commit -am 'Add some feature'`
4. 推送到分支 `git push origin feature/your-feature`
5. 创建 Pull Request

## 许可证

[MIT License](LICENSE)

# 分布式票务库存管理系统 (Distributed Ticketing Inventory Management System)

## 项目简介
本系统是一个高性能、高可用的分布式票务库存管理系统，专门用于处理演唱会、体育赛事等大型活动的票务资源分配与实时库存管理。系统采用微服务架构，通过分布式技术解决高并发场景下的各类挑战。

### 核心指标
- 单节点支持：50,000 QPS
- 平均响应时间：<50ms
- 库存准确性：99.99%
- 系统可用性：99.99%
- 故障转移时间：<3秒

## 系统架构

### 技术栈
- **微服务框架**: Spring Cloud Alibaba
  - Nacos：服务注册与发现
  - Seata：分布式事务管理
- **数据存储**: 
  - MySQL：核心数据存储
  - Redis：缓存层和分布式锁
  - ShardingJDBC：分库分表中间件
- **消息队列**：RocketMQ
- **监控系统**：Prometheus + Grafana
- **压测工具**：JMeter

### 核心模块
1. **库存管理服务 (Inventory Service)**
   - 库存分片管理
   - 库存状态维护
   - 库存变更记录

2. **订单服务 (Order Service)**
   - 订单创建和管理
   - 库存预占
   - 支付状态同步

3. **票务服务 (Ticket Service)**
   - 场次管理
   - 票种管理
   - 价格管理

4. **用户服务 (User Service)**
   - 用户认证
   - 权限管理
   - 用户操作记录

## 核心特性

### 1. 库存分片路由
- 基于用户ID哈希与票务场次ID的两级分片策略
- 16个物理分库设计
- 支持动态扩容

### 2. 分布式锁优化
- Redisson分段锁模式
- 20个虚拟库存段
- 细粒度锁控制

### 3. 缓存架构
- 二级缓存架构：本地缓存(Guava) + 分布式缓存(Redis)
- 热点数据实时探测
- 缓存预热策略

### 4. 异步库存处理
- 预占库存 + 异步确认的两阶段设计
- 批量合并更新
- 消息队列削峰

### 5. 监控告警
- 全链路监控
- 性能指标采集
- 实时告警机制

## 部署架构
- 多机房部署
- 双活数据中心
- 灾备切换方案

## 性能优化
1. **库存操作优化**
   - 分段锁降低锁粒度
   - 批量处理提升吞吐
   - 异步确认减少等待

2. **查询性能优化**
   - 多级缓存
   - 索引优化
   - 读写分离

## 安装部署
[待补充]

## 使用指南
[待补充]

## 开发指南
[待补充]

## 变更日志
[待补充]


#项目整体架构图
graph TB
    subgraph "客户端层"
        A1[Web前端]
        A2[移动端H5]
        A3[小程序]
    end

    subgraph "网关层"
        B1[Spring Cloud Gateway]
        B2[Sentinel限流]
        B3[JWT认证]
    end

    subgraph "微服务层"
        C1[用户服务]
        C2[票务服务]
        C3[订单服务]
        C4[支付服务]
        C5[库存服务]
        C6[退款服务]
    end

    subgraph "中间件层"
        D1[Nacos注册中心]
        D2[RocketMQ消息队列]
        D3[Seata分布式事务]
        D4[Redis分布式缓存]
    end

    subgraph "存储层"
        E1[MySQL主从]
        E2[Redis集群]
    end

    subgraph "监控层"
        F1[ELK日志分析]
        F2[Prometheus监控]
        F3[Grafana可视化]
    end

    A1 & A2 & A3 --> B1
    B1 --> B2 --> B3
    B3 --> C1 & C2 & C3 & C4 & C5 & C6
    C1 & C2 & C3 & C4 & C5 & C6 --> D1 & D2 & D3 & D4
    D1 & D2 & D3 & D4 --> E1 & E2
    C1 & C2 & C3 & C4 & C5 & C6 --> F1 & F2
    F2 --> F3 

#高并发抢票流程图
sequenceDiagram
    participant User as 用户
    participant Gateway as 网关层
    participant Redis as Redis缓存
    participant InventoryService as 库存服务
    participant OrderService as 订单服务
    participant MQ as RocketMQ
    participant DB as 数据库

    User->>Gateway: 发起抢票请求
    Gateway->>Gateway: 限流&认证
    Gateway->>Redis: 查询活动库存分片
    
    alt 有库存
        Redis->>InventoryService: 获取分布式锁
        InventoryService->>Redis: 预扣减库存
        InventoryService->>MQ: 发送创建订单消息
        MQ->>OrderService: 异步创建订单
        OrderService->>DB: 保存订单信息
        OrderService->>User: 返回订单号
    else 无库存
        Redis->>User: 返回售罄信息
    end 

#支付流程图
sequenceDiagram
    participant User as 用户
    participant OrderService as 订单服务
    participant PaymentService as 支付服务
    participant ThirdParty as 第三方支付
    participant MQ as RocketMQ
    participant InventoryService as 库存服务

    User->>OrderService: 发起支付
    OrderService->>PaymentService: 创建支付单
    PaymentService->>ThirdParty: 调用支付接口
    ThirdParty->>User: 返回支付二维码
    
    alt 支付成功
        ThirdParty->>PaymentService: 支付成功回调
        PaymentService->>MQ: 发送支付成功消息
        MQ->>OrderService: 更新订单状态
        MQ->>InventoryService: 确认扣减库存
    else 支付超时
        PaymentService->>OrderService: 取消订单
        OrderService->>InventoryService: 释放库存
    end 

#退款流程图
sequenceDiagram
    participant User as 用户
    participant RefundService as 退款服务
    participant OrderService as 订单服务
    participant PaymentService as 支付服务
    participant ThirdParty as 第三方支付
    participant MQ as RocketMQ

    User->>RefundService: 申请退款
    RefundService->>OrderService: 检查订单状态
    
    alt 可以退款
        RefundService->>PaymentService: 创建退款单
        PaymentService->>ThirdParty: 发起退款
        ThirdParty->>PaymentService: 退款结果回调
        PaymentService->>MQ: 发送退款结果消息
        MQ->>RefundService: 更新退款状态
        MQ->>OrderService: 更新订单状态
        RefundService->>User: 退款成功通知
    else 不可退款
        RefundService->>User: 返回拒绝原因
    end 

#库存分片设计图
graph LR
    subgraph "库存分片策略"
        A[总库存50000]
        B[分片大小1000]
        C[分片数量50]
    end

    subgraph "Redis集群"
        D1[分片1<br>0-999]
        D2[分片2<br>1000-1999]
        D3[分片3<br>2000-2999]
        D4[......]
        D5[分片50<br>49000-49999]
    end

    subgraph "加锁策略"
        E1[Redisson分布式锁]
        E2[每个分片独立加锁]
        E3[减少锁竞争]
    end

    A --> B
    B --> C
    C --> D1 & D2 & D3 & D4 & D5
    D1 & D2 & D3 & D4 & D5 --> E1
    E1 --> E2 --> E3 

#数据一致性保证图
graph TB
    subgraph "分布式事务"
        A[Seata AT模式]
        B[全局事务]
        C[分支事务]
    end

    subgraph "订单服务"
        D1[创建订单]
        D2[更新状态]
    end

    subgraph "库存服务"
        E1[扣减库存]
        E2[库存回滚]
    end

    subgraph "支付服务"
        F1[创建支付单]
        F2[支付确认]
    end

    A --> B
    B --> C
    C --> D1 & E1 & F1
    D1 --> D2
    E1 --> E2
    F1 --> F2 