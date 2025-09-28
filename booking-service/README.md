# Booking Service

## Overview
The Booking Service is responsible for managing flight bookings in the Flight Booking System. It handles the complete booking lifecycle including seat reservation, payment processing (mocked), and booking management.

## Features
- Create new bookings with multiple passengers
- Real-time seat availability checking
- Temporary seat blocking with Redis (10-minute TTL)
- Mock payment processing (always successful)
- Booking retrieval by PNR
- User booking history
- Booking cancellation (with 2-hour restriction before departure)
- Event-driven architecture with Kafka
- State-based booking flow (no complex transactions)

## Technology Stack
- **Framework**: Spring Boot 3.1.0 with WebFlux (Reactive)
- **Language**: Kotlin 1.8.22
- **Database**: PostgreSQL with R2DBC (reactive driver)
- **Cache**: Redis with Lettuce Core
- **Message Queue**: Apache Kafka
- **Build Tool**: Maven

## Architecture

### Booking Flow
1. **INITIATED** - Booking created
2. **PAYMENT_PENDING** - Awaiting payment
3. **PAYMENT_CONFIRMED** - Payment successful
4. **CONFIRMED** - Booking fully confirmed
5. **CANCELLED/FAILED** - Terminal states

### Key Components
- **BookingOrchestrator**: Main orchestration logic
- **BookingService**: Core CRUD operations
- **SeatBlockingCache**: Redis-based seat management
- **FlightManagementClient**: Integration with Flight Management Service
- **BookingEventPublisher**: Kafka event publishing

## API Endpoints

### Create Booking
```
POST /api/bookings
Content-Type: application/json

{
  "flightScheduleId": "uuid",
  "flightNumber": "AI101",
  "departureDate": "2024-12-25T10:00:00",
  "origin": "Delhi",
  "destination": "Mumbai",
  "passengers": [
    {
      "firstName": "John",
      "lastName": "Doe",
      "age": 30,
      "gender": "MALE",
      "idType": "PASSPORT",
      "idNumber": "A1234567"
    }
  ],
  "contactEmail": "john@example.com",
  "contactPhone": "+919876543210",
  "totalAmount": 5000.00
}
```

### Get Booking by PNR
```
GET /api/bookings/{pnr}
```

### Get User Bookings
```
GET /api/bookings/user/{email}
```

### Cancel Booking
```
PUT /api/bookings/{pnr}/cancel?reason=optional_reason
```

## Database Schema

### Tables
1. **bookings** - Main booking information
2. **passengers** - Passenger details
3. **booking_status_history** - Audit trail

## Redis Data Structure
```
# Seat availability
flight:schedule:{scheduleId}:seats
  - available: 150
  - blocked: 5
  - confirmed: 45

# Temporary seat blocking
booking:{bookingId}:seats
  - TTL: 10 minutes
```

## Kafka Topics
- **booking-events** - Booking lifecycle events
- **seat-update-events** - Seat inventory updates

## Configuration

### Application Properties
```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/booking_db
    username: postgres
    password: postgres
  
  redis:
    host: localhost
    port: 6379
  
  kafka:
    bootstrap-servers: localhost:9092

server:
  port: 8082

services:
  flight-management:
    url: http://localhost:8081
```

## Running the Service

### Prerequisites
- Java 17
- PostgreSQL
- Redis
- Kafka
- Maven

### Build and Run
```bash
# Build the project
mvn clean install

# Run the service
mvn spring-boot:run
```

### Docker Support
```bash
# Start dependencies
docker-compose up -d postgres redis kafka

# Run the application
mvn spring-boot:run
```

## Testing
```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify
```

## Performance Targets
- **Peak Load**: 300 bookings/second
- **Average Load**: 100 bookings/second
- **Response Time**: < 500ms for booking creation
- **Availability**: 99.9%

## Monitoring
- Health endpoint: `/api/bookings/health`
- Metrics: Micrometer integration (if needed)
- Logging: Structured JSON logs

## Future Enhancements
- Real payment gateway integration
- Seat selection feature
- Email/SMS notifications
- Booking modification
- Refund processing
- Multi-currency support
