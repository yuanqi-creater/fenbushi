@echo off
setlocal enabledelayedexpansion

echo ======================================
echo 分布式票务系统启动脚本
echo ======================================
echo.

:: 检查Java环境
echo 正在检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Java环境，请安装JDK 11
    echo 下载地址：https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html
    pause
    exit /b 1
)
echo [成功] Java环境检查通过
echo.

:: 检查Maven环境
echo 正在检查Maven环境...
mvn -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Maven环境，请安装Maven 3.8+
    echo 下载地址：https://maven.apache.org/download.cgi
    pause
    exit /b 1
)
echo [成功] Maven环境检查通过
echo.

:: 检查MySQL环境
echo 正在检查MySQL环境...
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到MySQL环境，请安装MySQL 8.0
    echo 下载地址：https://dev.mysql.com/downloads/installer/
    pause
    exit /b 1
)
echo [成功] MySQL环境检查通过
echo.

:: 检查Redis环境
echo 正在检查Redis服务...
tasklist /FI "IMAGENAME eq redis-server.exe" 2>NUL | find /I /N "redis-server.exe" >NUL
if %errorlevel% neq 0 (
    echo [提示] 正在启动Redis服务...
    start /B redis-server.exe
    timeout /t 5 /nobreak >nul
)
echo [成功] Redis服务已启动
echo.

:: 检查Node.js环境
echo 正在检查Node.js环境...
node -v >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Node.js环境，请安装Node.js
    echo 下载地址：https://nodejs.org/
    pause
    exit /b 1
)
echo [成功] Node.js环境检查通过
echo.

:: 初始化数据库
echo 正在初始化数据库...
set /p MYSQL_PASSWORD=请输入MySQL root密码: 
mysql -u root -p%MYSQL_PASSWORD% < sql/init.sql
if %errorlevel% neq 0 (
    echo [错误] 数据库初始化失败，请检查密码是否正确
    pause
    exit /b 1
)
echo [成功] 数据库初始化完成
echo.

:: 编译项目
echo 正在编译项目...
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo [错误] 项目编译失败
    pause
    exit /b 1
)
echo [成功] 项目编译完成
echo.

:: 启动后端服务
echo 正在启动后端服务...

start "Auth Service" cmd /c "cd auth-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

start "Inventory Service" cmd /c "cd inventory-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

start "Order Service" cmd /c "cd order-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

start "Payment Service" cmd /c "cd payment-service && mvn spring-boot:run"
timeout /t 5 /nobreak >nul

start "User Service" cmd /c "cd user-service && mvn spring-boot:run"
timeout /t 10 /nobreak >nul

echo [成功] 后端服务启动完成
echo.

:: 启动前端项目
echo 正在启动前端项目...
cd ticketing-web
call npm install
if %errorlevel% neq 0 (
    echo [错误] 前端依赖安装失败
    pause
    exit /b 1
)

start "Frontend" cmd /c "npm start"
cd ..

echo.
echo ======================================
echo 所有服务已启动完成！
echo 请访问 http://localhost:3000 使用系统
echo 默认管理员账号：admin
echo 默认密码：admin123
echo ======================================

pause 