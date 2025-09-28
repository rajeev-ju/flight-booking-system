# Circuit Breaker Implementation Guide for Flight Booking System

## Executive Summary
Circuit breakers are essential for building resilient microservices. They prevent cascading failures by detecting when a service is failing and providing fallback mechanisms. This guide identifies critical integration points in the Flight Booking System where circuit breakers should be implemented.

## Architecture Overview

```
┌─────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│                 │────▶│                  │────▶│                  │
│ Search Engine   │     │ Flight Management│     │    PostgreSQL    │
│    Service      │◀────│     Service      │◀────│    Database      │
└─────────────────┘     └──────────────────┘     └──────────────────┘
         │                       ▲
         │                       │
         ▼                       │
┌─────────────────┐              │
│                 │──────────────┘
│ Booking Service │
│                 │────▶ External Payment Gateway
└─────────────────┘────▶ External Email Service
```

## Critical Integration Points for Circuit Breakers

### 1. **Inter-Service Communication**

#### 1.1 Search Engine Service → Flight Management Service
**Location**: `/search-engine-service/src/main/kotlin/com/airline/search/client/FlightManagementClient.kt`

**Current State**: Basic error handling with fallback to empty responses
**Risk**: Service unavailability can cause degraded search experience

**Recommended Implementation**:
```kotlin
@Component
class FlightManagementClient(
    private val webClient: WebClient.Builder,
    @Value("\${services.flight-management.url}")
    private val flightServiceUrl: String
) {
    
    @CircuitBreaker(name = "flight-management", fallbackMethod = "fallbackGetAllActiveFlights")
    @TimeLimiter(name = "flight-management")
    @Retry(name = "flight-management")
    fun getAllActiveFlights(): Flux<Flight> {
        // Existing implementation
    }
    
    fun fallbackGetAllActiveFlights(exception: Exception): Flux<Flight> {
        logger.warn("Circuit breaker activated: Using cached flight data")
        return cacheService.getCachedFlights()
    }
}
```

**Circuit Breaker Configuration**:
- **Failure Rate Threshold**: 50%
- **Slow Call Duration**: 3 seconds
- **Slow Call Rate Threshold**: 50%
- **Minimum Number of Calls**: 10
- **Wait Duration in Open State**: 30 seconds
- **Permitted Calls in Half-Open**: 5

#### 1.2 Booking Service → Flight Management Service
**Location**: `/booking-service/src/main/kotlin/com/airline/booking/client/FlightManagementClient.kt`

**Critical Operations**:
- `checkSeatAvailability()` - Must fail fast
- `reserveSeats()` - Requires compensation logic
- `releaseSeats()` - Best effort, can retry
- `getFlightScheduleDetails()` - Can use cached data

**Recommended Implementation**:
```kotlin
@Component
class FlightManagementClient(
    private val webClient: WebClient,
    private val circuitBreakerFactory: CircuitBreakerFactory<*, *>
) {
    
    @CircuitBreaker(name = "seat-availability", fallbackMethod = "fallbackCheckSeatAvailability")
    @TimeLimiter(name = "seat-availability")
    suspend fun checkSeatAvailability(
        flightScheduleId: UUID,
        numberOfSeats: Int
    ): SeatAvailabilityResponse {
        // Existing implementation
    }
    
    suspend fun fallbackCheckSeatAvailability(
        flightScheduleId: UUID,
        numberOfSeats: Int,
        exception: Exception
    ): SeatAvailabilityResponse {
        logger.warn("Circuit breaker: Seat availability check failed for $flightScheduleId")
        // Return conservative response - seats not available
        return SeatAvailabilityResponse(
            available = false,
            availableSeats = 0,
            requestedSeats = numberOfSeats,
            reason = "Service temporarily unavailable"
        )
    }
    
    @CircuitBreaker(name = "seat-reservation")
    @Retry(name = "seat-reservation", fallbackMethod = "fallbackReserveSeats")
    suspend fun reserveSeats(
        flightScheduleId: UUID,
        numberOfSeats: Int
    ): Boolean {
        // Existing implementation with saga pattern
    }
}
```

### 2. **External Service Integration**

#### 2.1 Payment Gateway Integration
**Location**: `/booking-service/src/main/kotlin/com/airline/booking/service/PaymentService.kt`

**Risk**: Payment gateway failures can block booking completion

**Recommended Implementation**:
```kotlin
@Service
class PaymentService {
    
    @CircuitBreaker(name = "payment-gateway", fallbackMethod = "fallbackProcessPayment")
    @TimeLimiter(name = "payment-gateway")
    @Retry(name = "payment-gateway")
    suspend fun processPayment(
        bookingId: UUID,
        amount: BigDecimal,
        userEmail: String
    ): PaymentResult {
        // Actual payment gateway integration
    }
    
    suspend fun fallbackProcessPayment(
        bookingId: UUID,
        amount: BigDecimal,
        userEmail: String,
        exception: Exception
    ): PaymentResult {
        logger.error("Payment gateway circuit breaker activated for booking: $bookingId")
        
        // Queue for manual processing
        paymentQueueService.queueForManualProcessing(
            bookingId, amount, userEmail
        )
        
        return PaymentResult(
            success = false,
            transactionId = null,
            message = "Payment processing delayed. You will receive confirmation via email.",
            requiresManualIntervention = true
        )
    }
}
```

**Circuit Breaker Configuration**:
- **Failure Rate Threshold**: 30% (more sensitive for payments)
- **Slow Call Duration**: 5 seconds
- **Wait Duration in Open State**: 60 seconds
- **Retry Attempts**: 2
- **Retry Wait Duration**: 1 second

#### 2.2 Email Service Integration
**Location**: To be implemented in BookingService

**Risk**: Email service failures should not block booking process

**Recommended Implementation**:
```kotlin
@Service
class EmailService {
    
    @CircuitBreaker(name = "email-service", fallbackMethod = "fallbackSendEmail")
    @Async
    suspend fun sendBookingConfirmation(
        bookingId: UUID,
        userEmail: String,
        bookingDetails: BookingDetails
    ) {
        // Email sending logic
    }
    
    suspend fun fallbackSendEmail(
        bookingId: UUID,
        userEmail: String,
        bookingDetails: BookingDetails,
        exception: Exception
    ) {
        logger.warn("Email service unavailable, queueing for later delivery")
        emailQueueService.queueEmail(bookingId, userEmail, bookingDetails)
    }
}
```

### 3. **Database Connections**

#### 3.1 R2DBC Connection Pool Circuit Breaker
**Location**: All services with database connections

**Recommended Implementation**:
```yaml
spring:
  r2dbc:
    pool:
      max-size: 20
      initial-size: 5
      max-idle-time: 30m
      max-acquire-time: 3s  # Fail fast if can't acquire connection
      validation-query: SELECT 1
      
resilience4j:
  circuitbreaker:
    instances:
      database:
        failure-rate-threshold: 50
        slow-call-duration-threshold: 2s
        slow-call-rate-threshold: 50
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 30s
```

## Implementation Strategy

### Phase 1: Critical Path (Week 1-2)
1. **Booking Service → Flight Management Service**
   - Implement circuit breaker for seat reservation
   - Add compensation logic for failed reservations
   - Test with chaos engineering

2. **Payment Gateway Integration**
   - Implement circuit breaker with queuing fallback
   - Add manual intervention workflow
   - Set up monitoring and alerts

### Phase 2: Service Resilience (Week 3-4)
1. **Search Engine Service → Flight Management Service**
   - Implement circuit breaker with caching fallback
   - Add cache warming strategy
   - Monitor cache hit rates

2. **Database Connection Pools**
   - Configure connection pool circuit breakers
   - Implement health checks
   - Add connection pool metrics

### Phase 3: Non-Critical Services (Week 5)
1. **Email Service**
   - Implement async circuit breaker
   - Add retry queue
   - Monitor delivery rates

2. **Monitoring and Observability**
   - Set up Prometheus metrics
   - Create Grafana dashboards
   - Configure alerts

## Monitoring and Metrics

### Key Metrics to Track
```yaml
metrics:
  circuit-breaker:
    - circuit.breaker.state (CLOSED, OPEN, HALF_OPEN)
    - circuit.breaker.failure.rate
    - circuit.breaker.slow.call.rate
    - circuit.breaker.calls.success
    - circuit.breaker.calls.failed
    - circuit.breaker.calls.not-permitted
    
  performance:
    - response.time.p99
    - response.time.p95
    - response.time.p50
    - throughput.requests.per.second
    
  business:
    - bookings.failed.due.to.circuit.breaker
    - payments.queued.for.manual.processing
    - search.results.served.from.cache
```

### Alert Configuration
```yaml
alerts:
  critical:
    - name: "Circuit Breaker Open"
      condition: circuit.breaker.state == "OPEN"
      duration: 1m
      severity: HIGH
      
    - name: "High Failure Rate"
      condition: circuit.breaker.failure.rate > 30%
      duration: 5m
      severity: MEDIUM
      
    - name: "Payment Processing Delayed"
      condition: payments.queued > 10
      duration: 1m
      severity: HIGH
```

## Testing Strategy

### 1. Unit Tests
```kotlin
@Test
fun `should activate circuit breaker after threshold failures`() {
    // Given
    repeat(10) {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
    }
    
    // When
    val results = (1..15).map {
        runCatching { 
            flightManagementClient.checkSeatAvailability(UUID.randomUUID(), 2) 
        }
    }
    
    // Then
    assertTrue(results.takeLast(5).all { it.isFailure })
    verify(exactly = 0) { webClient.get() } // Last 5 calls not permitted
}
```

### 2. Integration Tests
```kotlin
@Test
@WithCircuitBreaker
fun `should handle cascading failures gracefully`() {
    // Simulate flight management service failure
    flightManagementServiceMock.simulateFailure()
    
    // Attempt booking
    val bookingResult = bookingService.createBooking(bookingRequest)
    
    // Verify graceful degradation
    assertFalse(bookingResult.success)
    assertEquals("Service temporarily unavailable", bookingResult.message)
    assertNotNull(bookingResult.retryAfter)
}
```

### 3. Chaos Engineering
- Use tools like Chaos Monkey or Litmus
- Simulate network partitions
- Test circuit breaker transitions
- Verify fallback mechanisms

## Best Practices

### 1. **Fail Fast**
- Set aggressive timeouts for critical operations
- Don't retry operations that are unlikely to succeed

### 2. **Graceful Degradation**
- Provide meaningful fallback responses
- Use cached data when possible
- Queue non-critical operations

### 3. **Bulkhead Pattern**
- Isolate circuit breakers by operation type
- Prevent one failure from affecting all operations
- Use separate thread pools

### 4. **Monitoring**
- Track all circuit breaker state changes
- Alert on sustained open states
- Monitor business impact metrics

### 5. **Testing**
- Regular chaos engineering exercises
- Load testing with failure scenarios
- Validate compensation logic

## Configuration Templates

### application.yml
```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 100
        failure-rate-threshold: 50
        slow-call-duration-threshold: 3s
        slow-call-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 10
        automatic-transition-from-open-to-half-open-enabled: true
        
      payment:
        failure-rate-threshold: 30
        slow-call-duration-threshold: 5s
        wait-duration-in-open-state: 60s
        
  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 1s
        retry-exceptions:
          - java.io.IOException
          - java.net.SocketTimeoutException
          
  timelimiter:
    configs:
      default:
        timeout-duration: 3s
        cancel-running-future: true
```

### Maven Dependencies
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-reactor</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-kotlin</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

## Conclusion

Implementing circuit breakers at these critical points will significantly improve the resilience of the Flight Booking System. The phased approach ensures that the most critical paths are protected first, while the comprehensive monitoring strategy provides visibility into system health and performance.

Priority should be given to:
1. Booking → Flight Management integration (critical for business)
2. Payment gateway integration (financial impact)
3. Search → Flight Management integration (user experience)

Regular testing and monitoring will ensure that circuit breakers are properly tuned and provide the expected protection against cascading failures.
