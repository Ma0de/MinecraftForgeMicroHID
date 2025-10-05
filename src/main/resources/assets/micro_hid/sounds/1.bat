@echo off
setlocal enabledelayedexpansion

REM 直接设置总时长（根据你的文件是8.8秒）
set /a total_seconds=8

echo 开始切割，总时长: !total_seconds! 秒

for /l %%i in (0,1,!total_seconds!) do (
    set /a idx=%%i+1
    echo 切割第 !idx! 秒...
    ffmpeg -y -i high_discharge.ogg -ss %%i -t 1 -c copy high_discharge_!idx!.ogg >nul 2>&1
)

echo 分割完成!
pause