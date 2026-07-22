@echo off
REM =============================================================================
REM DEPLOYMENT SCRIPT FOR WINDOWS - Employee Management System
REM =============================================================================
REM
REM This script deploys the EMS application using Docker Compose.
REM
REM USAGE:
REM   deploy.bat [command]
REM
REM COMMANDS:
REM   start    - Start all services
REM   stop     - Stop all services
REM   restart  - Restart all services
REM   logs     - View application logs
REM   build    - Build and start services
REM   clean    - Stop and remove all containers, volumes
REM   status   - Show status of services
REM
REM =============================================================================

setlocal enabledelayedexpansion

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not running. Please start Docker Desktop.
    exit /b 1
)

REM Set default command
set COMMAND=%1
if "%COMMAND%"=="" set COMMAND=start

REM Process commands
if "%COMMAND%"=="start" (
    echo [INFO] Starting EMS services...
    docker-compose up -d
    echo [INFO] Services started. Access the application at http://localhost:8080
    echo [INFO] Swagger UI: http://localhost:8080/swagger-ui.html
    goto :end
)

if "%COMMAND%"=="stop" (
    echo [INFO] Stopping EMS services...
    docker-compose down
    echo [INFO] Services stopped.
    goto :end
)

if "%COMMAND%"=="restart" (
    echo [INFO] Restarting EMS services...
    docker-compose restart
    echo [INFO] Services restarted.
    goto :end
)

if "%COMMAND%"=="logs" (
    echo [INFO] Showing logs (Ctrl+C to exit)...
    docker-compose logs -f
    goto :end
)

if "%COMMAND%"=="build" (
    echo [INFO] Building and starting EMS services...
    docker-compose up -d --build
    echo [INFO] Build complete. Services started.
    goto :end
)

if "%COMMAND%"=="clean" (
    echo [WARNING] This will remove all containers and volumes!
    set /p CONFIRM="Are you sure? (y/n): "
    if /i "!CONFIRM!"=="y" (
        echo [INFO] Cleaning up...
        docker-compose down -v --rmi local
        echo [INFO] Cleanup complete.
    ) else (
        echo [INFO] Cleanup cancelled.
    )
    goto :end
)

if "%COMMAND%"=="status" (
    echo [INFO] Service status:
    docker-compose ps
    echo.
    echo [INFO] Health check:
    docker-compose exec ems-app wget -qO- http://localhost:8080/actuator/health 2>nul || echo Health check not available
    goto :end
)

echo [ERROR] Unknown command: %COMMAND%
echo.
echo Available commands:
echo   start    - Start all services
echo   stop     - Stop all services
echo   restart  - Restart all services
echo   logs     - View application logs
echo   build    - Build and start services
echo   clean    - Stop and remove all containers, volumes
echo   status   - Show status of services

:end
endlocal
