# 项目问题与解决记录

## Maven构建问题

1. **问题**: Maven无法找到toolchains.xml配置
   - **解决**: 在用户主目录的.m2文件夹下创建toolchains.xml文件

2. **问题**: JDK 11安装路径配置错误
   - **解决**: 修改toolchains.xml中的JDK路径为`E:\Program Files\Java\jdk-11`

3. **问题**: Missing dependencies in common模块
   - **解决**: 添加Spring Cloud OpenFeign和Spring Cloud Alibaba Nacos Discovery依赖

4. **问题**: BusinessException类缺失
   - **解决**: 创建BusinessException类实现RuntimeException

5. **问题**: MySQL依赖artifactId配置错误
   - **解决**: 将`com.mysql:mysql-connector-j`更改为`mysql:mysql-connector-java`

6. **问题**: 用户服务缺少Spring Security依赖
   - **解决**: 添加Spring Boot Security依赖到user-service模块

## 前端问题

1. **问题**: 缺少前端项目的必要文件结构
   - **解决**: 创建public目录和index.html、manifest.json等文件

2. **问题**: 缺少qrcode.react依赖
   - **解决**: 安装qrcode.react依赖包

3. **问题**: QRCode组件导入方式错误
   - **解决**: 修改导入方式，从默认导入改为`import { QRCodeCanvas } from 'qrcode.react'`

## 构建命令参考

### Maven构建选项

```bash
# 跳过测试构建
mvn clean install -DskipTests

# 禁用编译失败时停止构建
mvn clean compile -Dmaven.compiler.failOnError=false

# 详细编译输出
mvn clean compile -Dmaven.compiler.verbose=true -X
```

### 前端构建选项

```bash
# 安装依赖
npm install

# 启动开发服务器
npm start

# 安装特定依赖
npm install qrcode.react --save
``` 