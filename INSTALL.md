# 分布式票务系统安装指南

## 环境要求

在运行本系统之前，请确保您的电脑已安装以下软件：

1. **JDK 11**
   - 下载地址：https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html
   - 安装后配置JAVA_HOME环境变量

2. **Maven 3.8+**
   - 下载地址：https://maven.apache.org/download.cgi
   - 安装后配置MAVEN_HOME环境变量

3. **MySQL 8.0**
   - 下载地址：https://dev.mysql.com/downloads/installer/
   - 安装时记住root密码
   - 确保MySQL服务已启动

4. **Redis**
   - 下载地址：https://github.com/microsoftarchive/redis/releases
   - 下载Redis-x64-3.0.504.zip
   - 解压后运行redis-server.exe

5. **Node.js**
   - 下载地址：https://nodejs.org/
   - 选择LTS版本安装

## 快速启动

1. 解压项目文件到任意目录

2. 双击运行 `start.bat`
   - 脚本会自动检查环境
   - 如果提示缺少某个组件，请按提示安装
   - 按要求输入MySQL密码

3. 等待所有服务启动完成
   - 会自动打开多个命令窗口
   - 请不要关闭这些窗口

4. 访问系统
   - 打开浏览器
   - 访问 http://localhost:3000
   - 默认管理员账号：admin
   - 默认密码：admin123

## 目录结构

```
fenbushi/
├── auth-service/        # 认证服务
├── inventory-service/   # 库存服务
├── order-service/      # 订单服务
├── payment-service/    # 支付服务
├── user-service/       # 用户服务
├── common/            # 公共模块
├── ticketing-web/     # 前端项目
├── sql/              # 数据库脚本
├── docs/             # 项目文档
├── start.bat         # 启动脚本
└── INSTALL.md        # 本文件
```

## 常见问题

1. **端口被占用**
   - 检查8080、3306、6379、3000等端口是否被占用
   - 使用 `netstat -ano | findstr "端口号"` 查看
   - 使用任务管理器关闭占用进程

2. **服务启动失败**
   - 检查MySQL服务是否启动
   - 检查Redis服务是否启动
   - 查看各服务日志定位问题

3. **数据库连接失败**
   - 确认MySQL密码正确
   - 确认MySQL服务正常运行
   - 检查数据库是否成功创建

4. **前端访问失败**
   - 确认Node.js安装正确
   - 检查3000端口是否被占用
   - 检查后端服务是否全部启动

## 手动启动步骤

如果自动启动脚本失败，可以按以下步骤手动启动：

1. **启动Redis**
```bash
cd C:\Redis
redis-server.exe
```

2. **初始化数据库**
```bash
mysql -u root -p < sql/init.sql
```

3. **编译项目**
```bash
mvn clean install -DskipTests
```

4. **启动后端服务**（每个命令都需要新开一个命令窗口）
```bash
cd auth-service
mvn spring-boot:run

cd inventory-service
mvn spring-boot:run

cd order-service
mvn spring-boot:run

cd payment-service
mvn spring-boot:run

cd user-service
mvn spring-boot:run
```

5. **启动前端项目**
```bash
cd ticketing-web
npm install
npm start
```

## 联系方式

如果遇到问题，请：
1. 查看项目文档
2. 检查错误日志
3. 提交Issue到项目仓库

## 注意事项

1. 首次运行可能需要较长时间
2. 请确保有足够的磁盘空间
3. 建议使用8GB以上内存的电脑
4. 所有命令窗口都不要关闭
5. 如需停止服务，直接关闭所有命令窗口即可 