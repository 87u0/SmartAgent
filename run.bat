@echo off
chcp 65001 >nul
setlocal

set JAVA_HOME=C:\tools\jdk-17.0.2
set MAVEN_HOME=C:\tools\apache-maven-3.9.6
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

REM 请修改下面的 Key 为你的智谱 API Key
set ZHIPU_API_KEY=你的智谱APIKey

cd /d %~dp0

echo ====================================
echo   SmartAgent - AI Agent Platform
echo ====================================
echo.
echo Java: %JAVA_HOME%
echo Maven: %MAVEN_HOME%
echo.
echo 启动中... 请稍候
echo 浏览器打开: http://localhost:8080
echo.

mvn spring-boot:run -q

pause
