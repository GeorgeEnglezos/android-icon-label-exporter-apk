@echo off
echo Building Icon Helper APK...
echo.

cd /d "%~dp0"

call gradlew.bat assembleDebug
if %ERRORLEVEL% neq 0 (
    echo.
    echo BUILD FAILED
    pause
    exit /b 1
)

echo.
echo Build successful!
echo APK: build\outputs\apk\debug\IconHelper-debug.apk
echo.
echo To install: adb install build\outputs\apk\debug\IconHelper-debug.apk
pause
