@echo off
setlocal enabledelayedexpansion

REM Ensure we operate from the repository root directory
set "ROOT_DIR=%~dp0"
cd /d "%ROOT_DIR%"

echo ========================================================
echo   Starting AgriCoop Java JDBC + MySQL Server
echo ========================================================
echo Working Directory: %CD%

REM Check if target/classes exists, compile if needed
if not exist "server-java\target\classes" (
    echo [INFO] Compiled classes not found. Building with Maven...
    call mvn -f server-java/pom.xml compile -q
    if errorlevel 1 (
        echo [ERROR] Maven compilation failed. Please check Java / Maven installation.
        pause
        exit /b 1
    )
)

echo [INFO] Launching AgriCoop Java Server on port 3000...
echo [INFO] Access the application at: http://localhost:3000
echo [INFO] Press Ctrl+C to stop the server.
echo ========================================================

java -cp "server-java/target/classes;server-java/lib/*" com.agricoop.AgriCoopServer
pause
