@echo off
REM 老人监护管理系统性能测试脚本
REM 作者: System
REM 版本: 1.0.0

setlocal enabledelayedexpansion

echo ========================================
echo 老人监护管理系统性能测试
echo ========================================

REM 设置变量
set JMETER_HOME=C:\apache-jmeter-5.6.3
set TEST_DIR=%~dp0
set REPORT_DIR=%TEST_DIR%\..\test-reports
set RESULTS_DIR=%TEST_DIR%\..\test-results

REM 创建目录
if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"
if not exist "%RESULTS_DIR%" mkdir "%RESULTS_DIR%"

REM 检查JMeter是否安装
if not exist "%JMETER_HOME%\bin\jmeter.bat" (
    echo [ERROR] JMeter未找到，请检查安装路径: %JMETER_HOME%
    pause
    exit /b 1
)

echo [INFO] 开始性能测试...
echo [INFO] 测试计划: elderly-monitoring-test.jmx
echo [INFO] 报告目录: %REPORT_DIR%

REM 运行性能测试
echo [INFO] 执行性能测试...
"%JMETER_HOME%\bin\jmeter.bat" -n -t "%TEST_DIR%\elderly-monitoring-test.jmx" -l "%RESULTS_DIR%\performance-results.jtl" -e -o "%REPORT_DIR%\performance-report"

if %errorlevel% equ 0 (
    echo [SUCCESS] 性能测试完成
    echo [INFO] 详细报告: %REPORT_DIR%\performance-report\index.html
) else (
    echo [ERROR] 性能测试失败
    pause
    exit /b 1
)

REM 生成性能分析报告
echo [INFO] 生成性能分析报告...
python "%TEST_DIR%\analyze-performance.py" "%RESULTS_DIR%\performance-results.jtl" "%REPORT_DIR%\performance-analysis.html"

if %errorlevel% equ 0 (
    echo [SUCCESS] 性能分析报告生成完成
) else (
    echo [WARNING] 性能分析报告生成失败
)

echo ========================================
echo 性能测试完成！
echo 报告位置:
echo   - 性能测试报告: %REPORT_DIR%\performance-report\index.html
echo   - 性能分析报告: %REPORT_DIR%\performance-analysis.html
echo ========================================

pause