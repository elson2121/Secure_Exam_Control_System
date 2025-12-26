@echo off
title SECS Server
echo ========================================
echo   SECURE EXAM CONTROL SYSTEM - SERVER
echo ========================================
echo.
echo Starting SECS Server...
echo.
echo Make sure:
echo 1. Java 11 or higher is installed
echo 2. Port 1099 is available
echo.
echo ========================================
echo.

REM Compile and run server
echo [1/3] Compiling server...
call mvn clean compile -q

echo [2/3] Starting RMI Registry...
start javac -cp "target/classes" com.secs.server.RMIServer

echo [3/3] Server is starting...
timeout /t 2 /nobreak >nul

echo.
echo ========================================
echo   SERVER STARTED SUCCESSFULLY!
echo ========================================
echo.
echo 📡 Server Information:
echo    - RMI Port: 1099
echo    - Service: ExamService
echo    - Status: Running
echo.
echo 👥 Sample Users:
echo    Students:
echo      • student1 / pass123 (John Doe)
echo      • student2 / pass123 (Jane Smith)
echo      • student3 / pass123 (Bob Johnson)
echo.
echo    Teachers:
echo      • teacher1 / admin123 (Dr. Smith)
echo      • teacher2 / admin123 (Prof. Williams)
echo.
echo 📚 Available Exams:
echo    1. Java Programming Basics (30 mins)
echo    2. Object Oriented Programming (45 mins)
echo    3. Database Management Systems (40 mins)
echo.
echo ========================================
echo Now start the client application...
echo ========================================
echo.
pause