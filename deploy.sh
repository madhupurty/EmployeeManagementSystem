#!/bin/bash
# =============================================================================
# DEPLOYMENT SCRIPT FOR LINUX/MAC - Employee Management System
# =============================================================================
#
# This script deploys the EMS application using Docker Compose.
#
# USAGE:
#   ./deploy.sh [command]
#
# COMMANDS:
#   start    - Start all services
#   stop     - Stop all services
#   restart  - Restart all services
#   logs     - View application logs
#   build    - Build and start services
#   clean    - Stop and remove all containers, volumes
#   status   - Show status of services
#
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    log_error "Docker is not running. Please start Docker."
    exit 1
fi

# Default command
COMMAND=${1:-start}

case $COMMAND in
    start)
        log_info "Starting EMS services..."
        docker-compose up -d
        log_info "Services started."
        log_info "Application: http://localhost:8080"
        log_info "Swagger UI: http://localhost:8080/swagger-ui.html"
        ;;
    
    stop)
        log_info "Stopping EMS services..."
        docker-compose down
        log_info "Services stopped."
        ;;
    
    restart)
        log_info "Restarting EMS services..."
        docker-compose restart
        log_info "Services restarted."
        ;;
    
    logs)
        log_info "Showing logs (Ctrl+C to exit)..."
        docker-compose logs -f
        ;;
    
    build)
        log_info "Building and starting EMS services..."
        docker-compose up -d --build
        log_info "Build complete. Services started."
        ;;
    
    clean)
        log_warn "This will remove all containers and volumes!"
        read -p "Are you sure? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            log_info "Cleaning up..."
            docker-compose down -v --rmi local
            log_info "Cleanup complete."
        else
            log_info "Cleanup cancelled."
        fi
        ;;
    
    status)
        log_info "Service status:"
        docker-compose ps
        echo
        log_info "Health check:"
        curl -s http://localhost:8080/actuator/health 2>/dev/null || echo "Health check not available"
        ;;
    
    *)
        log_error "Unknown command: $COMMAND"
        echo
        echo "Available commands:"
        echo "  start    - Start all services"
        echo "  stop     - Stop all services"
        echo "  restart  - Restart all services"
        echo "  logs     - View application logs"
        echo "  build    - Build and start services"
        echo "  clean    - Stop and remove all containers, volumes"
        echo "  status   - Show status of services"
        exit 1
        ;;
esac
