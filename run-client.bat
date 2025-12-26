@echo off
title SECS Client
echo ========================================
echo   SECURE EXAM CONTROL SYSTEM - CLIENT
echo ========================================
echo.
echo Starting SECS Client...
echo.
echo Prerequisites:
echo 1. Server must be running first
echo 2. Java 11 or higher with JavaFX
echo 3. Make sure server is on localhost:1099
echo.
echo ========================================
echo.

REM Set JavaFX path (modify this to your JavaFX SDK location)
set JAVA_FX_PATH="C:\path\to\javafx-sdk\lib"

REM Check if JavaFX path exists
if not exist %JAVA_FX_PATH% (
    echo ⚠️  JavaFX not found at: %JAVA_FX_PATH%
    echo Please modify the JAVA_FX_PATH in this batch file
    echo.
    pause
    exit /b 1
)

REM Compile project
echo [1/3] Compiling project...
call mvn clean compile -q

echo [2/3] Starting client application...
echo.

REM Run the client
java --module-path %JAVA_FX_PATH% --add-modules javafx.controls,javafx.fxml -cp "target/classes" com.secs.client.MainApp

echo.
echo ========================================
echo   CLIENT SHUTDOWN
echo ========================================
echo.
pause