# 🚀 Flight Booking System - Complete Running Guide

This guide provides detailed instructions for setting up, running, and testing the Flight Booking System.

## 📋 Table of Contents
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the System](#running-the-system)
- [Testing](#testing)
- [API Examples](#api-examples)
- [Troubleshooting](#troubleshooting)
- [Development Tips](#development-tips)

## Prerequisites

### System Requirements
- **OS**: Linux, macOS, or Windows with WSL2
- **RAM**: Minimum 8GB (16GB recommended)
- **Disk Space**: 10GB free space
- **CPU**: 4+ cores recommended

### Software Requirements
```bash
# Check Java version (21 required)
java -version

# Check Maven version (3.9+ required)
mvn -version

# Check Docker version
docker --version
docker-compose --version

# Check available memory
docker system info | grep Memory
```

### Install Prerequisites

#### macOS
```bash
# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java 21
brew install openjdk@21

# Install Maven
brew install maven

# Install Docker Desktop
brew install --cask docker
```

#### Linux (Ubuntu/Debian)
```bash
# Update package list
sudo apt update

# Install Java 21
sudo apt install openjdk-21-jdk

# Install Maven
sudo apt install maven

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

## Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/flightbookingsystem.git
cd flightbookingsystem
```

### 2. Build the Project
```bash
# Build all modules
mvn clean install

# Build without tests (faster)
mvn clean install -DskipTests

# Build specific module
mvn clean install -pl booking-service -am
```

## Running the System

### 🎯 Option 1: One-Command Setup (Recommended)

```bash
# Make the script executable
chmod +x run-and-test.sh

# Run everything (build, deploy, test)
./run-and-test.sh
```

This script will:
1. ✅ Build all services with Maven
2. ✅ Start PostgreSQL, Redis, Elasticsearch, Kafka
3. ✅ Run database migrations
4. ✅ Start all microservices
5. ✅ Wait for health checks
6. ✅ Run comprehensive tests
7. ✅ Verify Kafka event processing

### 🔧 Option 2: Step-by-Step Manual Setup

#### Step 1: Start Infrastructure Services
```bash
# Start only infrastructure services
docker-compose up -d postgres redis elasticsearch kafka zookeeper

# Wait for them to be ready
sleep 30

# Verify they're running
docker-compose ps
```

#### Step 2: Run Database Migrations
```bash
# Run Liquibase migrations
docker-compose up database-migrations

# Verify tables were created
docker exec flight-postgres psql -U postgres -d flight_management -c "\dt"
```

#### Step 3: Start Microservices
```bash
# Start all microservices
docker-compose up -d flight-management-service search-engine-service booking-service

# Or start individually
docker-compose up -d flight-management-service
docker-compose up -d search-engine-service
docker-compose up -d booking-service
```

#### Step 4: Verify Services
```bash
# Check all services are running
docker-compose ps

# Check health endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health

# View logs
docker-compose logs -f flight-management-service
docker-compose logs -f search-engine-service
docker-compose logs -f booking-service
```

### 🐳 Option 3: Docker-Only Setup

```bash
# Build and start everything
docker-compose up --build

# Run in background
docker-compose up -d --build

# Scale services
docker-compose up -d --scale search-engine-service=2
```

## Testing

### 🧪 Automated Test Suite

```bash
# Run the complete test flow
./run-and-test.sh

# The script tests:
# ✅ Flight search functionality
# ✅ Booking creation
# ✅ Seat management via Kafka
# ✅ Multi-route searches
```

### 🔍 Manual Testing

#### 1. Search for Flights
```bash
curl -X POST http://localhost:8080/search-engine-service/api/v1/flights/search \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "DEL",
    "destination": "BOM",
    "departureDate": "2025-10-01",
    "passengers": 2,
    "sortBy": "PRICE",
    "maxResults": 10,
    "includeConnecting": false,
    "cabinClass": "ECONOMY"
  }' | jq
```

#### 2. Get Flight Schedule Details
```bash
# Replace {scheduleId} with actual UUID from search results
curl http://localhost:8081/flight-management-service/api/schedules/{scheduleId} | jq
```

#### 3. Create a Booking
```bash
curl -X POST http://localhost:8082/booking-system/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "flightScheduleId": "schedule-uuid-here",
    "flightNumber": "AI101",
    "passengers": [
      {
        "firstName": "John",
        "lastName": "Doe",
        "age": 30,
        "gender": "MALE",
        "idType": "PASSPORT",
        "idNumber": "P123456789"
      }
    ],
    "contactEmail": "john.doe@example.com",
    "contactPhone": "+919876543210",
    "totalAmount": 4500.00
  }' | jq
```

#### 4. Verify Seat Reduction
```bash
# Check database directly
docker exec flight-postgres psql -U postgres -d flight_management \
  -c "SELECT flight_number, available_seats FROM flight_schedules WHERE id = 'schedule-uuid-here';"

# Check via API
curl http://localhost:8081/flight-management-service/api/schedules/{scheduleId} | jq '.availableSeats'
```

### 📊 Load Testing

```bash
# Install Apache Bench
sudo apt-get install apache2-utils  # Linux
brew install httpd  # macOS

# Test search endpoint
ab -n 1000 -c 10 -p search-request.json -T application/json \
  http://localhost:8080/search-engine-service/api/v1/flights/search

# Test with different concurrency levels
for c in 1 5 10 20 50; do
  echo "Testing with $c concurrent requests"
  ab -n 100 -c $c -p search-request.json -T application/json \
    http://localhost:8080/search-engine-service/api/v1/flights/search
done
```

## API Examples

### Complete Booking Flow Example

```bash
#!/bin/bash

# 1. Search for flights
echo "Searching for flights..."
SEARCH_RESPONSE=$(curl -s -X POST http://localhost:8080/search-engine-service/api/v1/flights/search \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "DEL",
    "destination": "BOM",
    "departureDate": "2025-10-01",
    "passengers": 1
  }')

# 2. Extract first flight details
SCHEDULE_ID=$(echo $SEARCH_RESPONSE | jq -r '.data.results[0].id' | cut -d'_' -f2)
FLIGHT_NUMBER=$(echo $SEARCH_RESPONSE | jq -r '.data.results[0].flights[0].flightNumber')
PRICE=$(echo $SEARCH_RESPONSE | jq -r '.data.results[0].totalPrice')

echo "Found flight $FLIGHT_NUMBER with schedule ID: $SCHEDULE_ID, Price: $PRICE"

# 3. Create booking
echo "Creating booking..."
BOOKING_RESPONSE=$(curl -s -X POST http://localhost:8082/booking-system/api/bookings \
  -H "Content-Type: application/json" \
  -d "{
    \"flightScheduleId\": \"$SCHEDULE_ID\",
    \"flightNumber\": \"$FLIGHT_NUMBER\",
    \"passengers\": [{
      \"firstName\": \"Test\",
      \"lastName\": \"User\",
      \"age\": 30,
      \"gender\": \"MALE\",
      \"idType\": \"PASSPORT\",
      \"idNumber\": \"P$(date +%s)\"
    }],
    \"contactEmail\": \"test@example.com\",
    \"contactPhone\": \"+919876543210\",
    \"totalAmount\": $PRICE
  }")

PNR=$(echo $BOOKING_RESPONSE | jq -r '.pnr')
echo "Booking created successfully! PNR: $PNR"
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Services Won't Start
```bash
# Check Docker daemon
sudo systemctl status docker

# Check port conflicts
lsof -i :8080
lsof -i :8081
lsof -i :8082

# Clean restart
docker-compose down -v
docker-compose up -d
```

#### 2. Database Connection Issues
```bash
# Check PostgreSQL logs
docker logs flight-postgres

# Test connection
docker exec flight-postgres psql -U postgres -c "\l"

# Reset database
docker-compose down -v
docker-compose up -d postgres
docker-compose up database-migrations
```

#### 3. Kafka Issues
```bash
# Check Kafka logs
docker logs flight-kafka

# List topics
docker exec flight-kafka kafka-topics.sh --list --bootstrap-server localhost:9092

# Check consumer groups
docker exec flight-kafka kafka-consumer-groups.sh --list --bootstrap-server localhost:9092

# Reset Kafka
docker-compose down -v
docker-compose up -d zookeeper kafka
sleep 10
docker-compose up kafka-init
```

#### 4. Elasticsearch Issues
```bash
# Check ES health
curl http://localhost:9200/_cluster/health?pretty

# Check indices
curl http://localhost:9200/_cat/indices?v

# Reset Elasticsearch
docker-compose down -v
docker-compose up -d elasticsearch
```

#### 5. Out of Memory
```bash
# Increase Docker memory (Docker Desktop)
# Go to Settings > Resources > Memory: 8GB minimum

# Check current usage
docker stats

# Clean up unused resources
docker system prune -a
```

## Development Tips

### 🔄 Hot Reload for Development

```bash
# Run services locally with hot reload
mvn spring-boot:run -pl booking-service

# Or use IDE's run configuration with:
# - Enable "Build project automatically"
# - Enable "Allow auto-make to start"
```

### 📝 Viewing Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f booking-service

# Last 100 lines
docker-compose logs --tail=100 booking-service

# Filter by timestamp
docker-compose logs --since="2024-01-01T00:00:00"
```

### 🗄️ Database Access

```bash
# Connect to PostgreSQL
docker exec -it flight-postgres psql -U postgres -d flight_management

# Useful queries
\dt                          # List tables
\d flight_schedules         # Describe table
SELECT * FROM flights LIMIT 10;
SELECT COUNT(*) FROM bookings;
```

### 🔍 Debugging

```bash
# Enable debug logging
export LOGGING_LEVEL_COM_AIRLINE=DEBUG

# Remote debugging (add to docker-compose.yml)
environment:
  - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
ports:
  - "5005:5005"  # Debug port
```

### 🧹 Cleanup

```bash
# Stop all services
docker-compose down

# Remove all data (fresh start)
docker-compose down -v

# Remove all containers and images
docker-compose down --rmi all

# Clean Maven cache
mvn clean
rm -rf ~/.m2/repository/com/airline
```

## 📚 Additional Resources

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Elasticsearch Guide](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- [Docker Compose Reference](https://docs.docker.com/compose/compose-file/)

---

**Need help?** Check the main [README.md](README.md) or raise an issue on GitHub.
