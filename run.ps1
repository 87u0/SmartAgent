$env:JAVA_HOME = "C:\tools\jdk-17.0.2"
$env:MAVEN_HOME = "C:\tools\apache-maven-3.9.6"
$env:Path = "$env:JAVA_HOME\bin;$env:MAVEN_HOME\bin;$env:Path"

# 修改成你的智谱 API Key
$env:ZHIPU_API_KEY = "你的智谱APIKey"

Set-Location $PSScriptRoot

Write-Host "====================================" -ForegroundColor Cyan
Write-Host "  SmartAgent - AI Agent Platform" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "启动中... 请稍候"
Write-Host "浏览器打开: http://localhost:8080"
Write-Host ""

mvn spring-boot:run -q

Read-Host "按回车退出"
