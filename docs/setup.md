# 开发环境搭建指南

本文档将指导你如何搭建分布式票务系统的开发环境。

## 1. 基础环境要求

### 1.1 Java开发环境
```bash
# 下载JDK 11
访问：https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html
下载：Windows x64 Installer

# 安装JDK
1. 运行下载的安装包
2. 记住安装路径（例如：C:\Program Files\Java\jdk-11）

# 配置环境变量
1. 右键"此电脑" -> 属性 -> 高级系统设置 -> 环境变量
2. 新建系统变量：
   变量名：JAVA_HOME
   变量值：C:\Program Files\Java\jdk-11
3. 编辑Path变量，添加：%JAVA_HOME%\bin

# 验证安装
打开命令提示符，输入：
java -version
javac -version
```

### 1.2 Maven配置
```bash
# 下载Maven
访问：https://maven.apache.org/download.cgi
下载：apache-maven-3.8.8-bin.zip

# 安装Maven
1. 解压到指定目录（例如：C:\Program Files\Maven）
2. 配置环境变量：
   变量名：MAVEN_HOME
   变量值：C:\Program Files\Maven
3. 编辑Path变量，添加：%MAVEN_HOME%\bin

# 验证安装
mvn -version

# 配置Maven镜像
编辑 %MAVEN_HOME%\conf\settings.xml，添加阿里云镜像：
<mirrors>
    <mirror>
        <id>alimaven</id>
        <name>aliyun maven</name>
        <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
        <mirrorOf>central</mirrorOf>
    </mirror>
</mirrors>
```

### 1.3 MySQL安装
```bash
# 下载MySQL 8.0
访问：https://dev.mysql.com/downloads/installer/
下载：MySQL Installer

# 安装步骤
1. 运行安装程序，选择"Server only"
2. 配置root密码（请记住此密码！）
3. 配置服务名称：MySQL80
4. 启动MySQL服务

# 验证安装
mysql -u root -p
输入密码
```

### 1.4 Redis安装
```bash
# 下载Redis for Windows
访问：https://github.com/microsoftarchive/redis/releases
下载：Redis-x64-3.0.504.zip

# 安装步骤
1. 解压到指定目录（例如：C:\Redis）
2. 安装服务：
   cd C:\Redis
   redis-server.exe --service-install

# 启动Redis
net start Redis

# 验证安装
redis-cli
ping
# 应返回PONG
```

### 1.5 Node.js安装
```bash
# 下载Node.js
访问：https://nodejs.org/
下载：LTS版本

# 安装步骤
1. 运行安装程序，接受所有默认设置
2. 自动配置环境变量

# 验证安装
node -v
npm -v

# 配置npm镜像
npm config set registry https://registry.npm.taobao.org
```

## 2. 中间件安装

### 2.1 Nacos安装
```bash
# 下载Nacos
访问：https://github.com/alibaba/nacos/releases
下载：nacos-server-2.0.3.zip

# 安装步骤
1. 解压到指定目录（例如：D:\nacos）
2. 修改配置：
   编辑 conf/application.properties
   spring.datasource.platform=mysql
   db.url.0=jdbc:mysql://localhost:3306/nacos?...
   db.user=root
   db.password=你的密码

# 启动Nacos
cd bin
startup.cmd

# 验证安装
访问：http://localhost:8848/nacos
默认账号密码：nacos/nacos
```

### 2.2 RocketMQ安装
```bash
# 下载RocketMQ
访问：https://rocketmq.apache.org/download
下载：rocketmq-all-4.9.3-bin-release.zip

# 安装步骤
1. 解压到指定目录（例如：D:\rocketmq）
2. 配置环境变量：
   ROCKETMQ_HOME=D:\rocketmq

# 启动Name Server
cd bin
start mqnamesrv.cmd

# 启动Broker
start mqbroker.cmd -n localhost:9876

# 验证安装
tools.cmd org.apache.rocketmq.example.quickstart.Producer
```

## 3. IDE配置

### 3.1 IntelliJ IDEA
```bash
# 下载IDEA
访问：https://www.jetbrains.com/idea/download
下载：Community版本（免费）或Ultimate版本（付费）

# 推荐插件
- Lombok
- Spring Assistant
- Maven Helper
- MyBatisX
```

### 3.2 VSCode
```bash
# 下载VSCode
访问：https://code.visualstudio.com/
下载：System Installer

# 推荐插件
- ESLint
- Prettier
- React Extension Pack
- GitLens
```

## 4. 项目初始化

### 4.1 克隆项目
```bash
git clone [项目地址]
cd fenbushi
```

### 4.2 初始化数据库
```bash
# 创建数据库
mysql -u root -p < sql/init.sql
```

### 4.3 启动服务
```bash
# 编译项目
mvn clean install

# 启动服务（按顺序）
1. 启动Nacos
2. 启动RocketMQ
3. 启动Redis
4. 启动各个微服务：
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

### 4.4 启动前端项目
```bash
cd ticketing-web
npm install
npm start
```

## 5. 开发建议

1. **代码规范**
   - 遵循阿里巴巴Java开发手册
   - 使用ESLint和Prettier格式化代码
   - 提交前进行代码审查

2. **分支管理**
   - main：主分支，保持稳定
   - develop：开发分支
   - feature/*：新功能分支
   - bugfix/*：bug修复分支

3. **提交规范**
   ```
   feat: 新功能
   fix: 修复bug
   docs: 文档更新
   style: 代码格式化
   refactor: 重构
   test: 测试用例
   chore: 其他修改
   ```

4. **日志规范**
   - 使用SLF4J + Logback
   - 分级别使用日志
   - 敏感信息脱敏

5. **异常处理**
   - 统一异常处理
   - 自定义业务异常
   - 详细错误信息

## 6. 常见问题

### 6.1 端口占用
```bash
# Windows查看端口占用
netstat -ano | findstr "8080"

# 结束进程
taskkill /F /PID 进程号
```

### 6.2 MySQL连接问题
```bash
# 检查服务状态
net start MySQL80

# 重置密码
mysqladmin -u root -p password 新密码
```

### 6.3 Redis连接问题
```bash
# 检查服务状态
redis-cli ping

# 清空数据
redis-cli flushall
```

### 6.4 Maven依赖问题
```bash
# 清理Maven缓存
mvn clean install -DskipTests
mvn dependency:purge-local-repository

# 更新项目
右键项目 -> Maven -> Update Project
```

## 7. 参考资源

1. [Spring Cloud Alibaba文档](https://spring-cloud-alibaba-group.github.io/github-pages/hoxton/en-us/index.html)
2. [Nacos文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
3. [RocketMQ文档](http://rocketmq.apache.org/docs/quick-start/)
4. [React文档](https://reactjs.org/docs/getting-started.html)
5. [Ant Design文档](https://ant.design/docs/react/introduce) 