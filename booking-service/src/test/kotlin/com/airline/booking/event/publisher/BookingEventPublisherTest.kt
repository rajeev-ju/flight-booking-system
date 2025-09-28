package com.airline.booking.event.publisher

import com.airline.booking.enums.BookingStatus
import com.airline.booking.event.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.test.assertTrue

class BookingEventPublisherTest {

    private lateinit var kafkaTemplate: KafkaTemplate<String, String>
    private lateinit var objectMapper: ObjectMapper
    private lateinit var bookingEventPublisher: BookingEventPublisher

    @BeforeEach
    fun setUp() {
        kafkaTemplate = mockk()
        objectMapper = mockk()
        bookingEventPublisher = BookingEventPublisher(kafkaTemplate, objectMapper)
    }

    @Test
    fun `should successfully publish BookingCreatedEvent to Kafka topic`() = runBlocking {
        // Given
        val event = BookingCreatedEvent(
            bookingId = UUID.randomUUID(),
            pnr = "ABC123",
            flightScheduleId = UUID.randomUUID(),
            flightNumber = "AI101",
            numberOfSeats = 2,
            totalAmount = BigDecimal("10000"),
            userEmail = "test@example.com",
            bookingStatus = BookingStatus.CONFIRMED,
            departureDate = LocalDateTime.now().plusDays(7),
            origin = "DEL",
            destination = "BOM"
        )
        val eventJson = """{"bookingId":"${event.bookingId}","pnr":"ABC123"}"""
        val sendResult = mockk<SendResult<String, String>>()
        val future = CompletableFuture.completedFuture(sendResult)
        
        every { objectMapper.writeValueAsString(event) } returns eventJson
        every { 
            kafkaTemplate.send("booking-events", event.bookingId.toString(), eventJson) 
        } returns future

        // When
        bookingEventPublisher.publishBookingCreated(event)

        // Then
        verify(exactly = 1) { objectMapper.writeValueAsString(event) }
        verify(exactly = 1) { 
            kafkaTemplate.send("booking-events", event.bookingId.toString(), eventJson) 
        }
    }

    @Test
    fun `should handle exception when publishing BookingCreatedEvent fails`() = runBlocking {
        // Given
        val event = BookingCreatedEvent(
            bookingId = UUID.randomUUID(),
            pnr = "ABC123",
            flightScheduleId = UUID.randomUUID(),
            flightNumber = "AI101",
            numberOfSeats = 2,
            totalAmount = BigDecimal("10000"),
            userEmail = "test@example.com",
            bookingStatus = BookingStatus.CONFIRMED,
            departureDate = LocalDateTime.now().plusDays(7),
            origin = "DEL",
            destination = "BOM"
        )
        
        every { objectMapper.writeValueAsString(event) } throws RuntimeException("Serialization error")

        // When
        bookingEventPublisher.publishBookingCreated(event)

        // Then - Should not throw exception
        verify(exactly = 1) { objectMapper.writeValueAsString(event) }
        verify(exactly = 0) { kafkaTemplate.send(any(), any(), any()) }
        assertTrue(true) // Test passes if no exception is thrown
    }

    @Test
    fun `should successfully publish BookingCancelledEvent to Kafka topic`() = runBlocking {
        // Given
        val event = BookingCancelledEvent(
            bookingId = UUID.randomUUID(),
            pnr = "ABC123",
            flightScheduleId = UUID.randomUUID(),
            numberOfSeats = 2,
            refundAmount = BigDecimal("9500"),
            cancellationReason = "Customer request"
        )
        val eventJson = """{"bookingId":"${event.bookingId}","pnr":"ABC123"}"""
        val sendResult = mockk<SendResult<String, String>>()
        val future = CompletableFuture.completedFuture(sendResult)
        
        every { objectMapper.writeValueAsString(event) } returns eventJson
        every { 
            kafkaTemplate.send("booking-events", event.bookingId.toString(), eventJson) 
        } returns future

        // When
        bookingEventPublisher.publishBookingCancelled(event)

        // Then
        verify(exactly = 1) { objectMapper.writeValueAsString(event) }
        verify(exactly = 1) { 
            kafkaTemplate.send("booking-events", event.bookingId.toString(), eventJson) 
        }
    }

    @Test
    fun `should successfully publish SeatUpdateEvent with CONFIRM operation`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val bookingId = UUID.randomUUID()
        val event = SeatUpdateEvent(
            flightScheduleId = flightScheduleId,
            operation = SeatOperation.CONFIRM,
            numberOfSeats = 2,
            bookingId = bookingId
        )
        val eventJson = """{"flightScheduleId":"$flightScheduleId","operation":"CONFIRM"}"""
        val sendResult = mockk<SendResult<String, String>>()
        val future = CompletableFuture.completedFuture(sendResult)
        
        every { objectMapper.writeValueAsString(event) } returns eventJson
        every { 
            kafkaTemplate.send("seat-updates", flightScheduleId.toString(), eventJson) 
        } returns future

        // When
        bookingEventPublisher.publishSeatUpdate(event)

        // Then
        verify(exactly = 1) { objectMapper.writeValueAsString(event) }
        verify(exactly = 1) { 
            kafkaTemplate.send("seat-updates", flightScheduleId.toString(), eventJson) 
        }
    }

    @Test
    fun `should successfully publish SeatUpdateEvent with RELEASE operation`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val bookingId = UUID.randomUUID()
        val event = SeatUpdateEvent(
            flightScheduleId = flightScheduleId,
            operation = SeatOperation.RELEASE,
            numberOfSeats = 2,
            bookingId = bookingId
        )
        val eventJson = """{"flightScheduleId":"$flightScheduleId","operation":"RELEASE"}"""
        val sendResult = mockk<SendResult<String, String>>()
        val future = CompletableFuture.completedFuture(sendResult)
        
        every { objectMapper.writeValueAsString(event) } returns eventJson
        every { 
            kafkaTemplate.send("seat-updates", flightScheduleId.toString(), eventJson) 
        } returns future

        // When
        bookingEventPublisher.publishSeatUpdate(event)

        // Then
        verify(exactly = 1) { objectMapper.writeValueAsString(event) }
        verify(exactly = 1) { 
            kafkaTemplate.send("seat-updates", flightScheduleId.toString(), eventJson) 
        }
    }

    @Test
    fun `should handle exception when publishing SeatUpdateEvent fails`() = runBlocking {
        // Given
        val event = SeatUpdateEvent(
            flightScheduleId = UUID.randomUUID(),
            operation = SeatOperation.CONFIRM,
            numberOfSeats = 2,
            bookingId = UUID.randomUUID()
        )
        
        every { objectMapper.writeValueAsString(event) } throws RuntimeException("Kafka error")

        // When
        bookingEventPublisher.publishSeatUpdate(event)

        // Then - Should not throw exception
        verify(exactly = 1) { objectMapper.writeValueAsString(event) }
        verify(exactly = 0) { kafkaTemplate.send(any(), any(), any()) }
        assertTrue(true) // Test passes if no exception is thrown
    }

    @Test
    fun `should successfully publish BookingStatusChangedEvent to Kafka topic`() = runBlocking {
        // Given
        val event = BookingStatusChangedEvent(
            bookingId = UUID.randomUUID(),
            pnr = "ABC123",
            oldStatus = BookingStatus.INITIATED,
            newStatus = BookingStatus.CONFIRMED,
            reason = "Payment successful"
        )
        val eventJson = """{"bookingId":"${event.bookingId}","oldStatus":"INITIATED","newStatus":"CONFIRMED"}"""
        val sendResult = mockk<SendResult<String, String>>()
        val future = CompletableFuture.completedFuture(sendResult)
        
        every { objectMapper.writeValueAsString(event) } returns eventJson
        every { 
            kafkaTemplate.send("booking-events", event.bookingId.toString(), eventJson) 
        } returns future

        // When
        bookingEventPublisher.publishBookingStatusChanged(event)

        // Then
        verify(exactly = 1) { objectMapper.writeValueAsString(event) }
        verify(exactly = 1) { 
            kafkaTemplate.send("booking-events", event.bookingId.toString(), eventJson) 
        }
    }

    @Test
    fun `should use correct Kafka topic for different event types`() = runBlocking {
        // Given
        val bookingEvent = BookingCreatedEvent(
            bookingId = UUID.randomUUID(),
            pnr = "ABC123",
            flightScheduleId = UUID.randomUUID(),
            flightNumber = "AI101",
            numberOfSeats = 2,
            totalAmount = BigDecimal("10000"),
            userEmail = "test@example.com",
            bookingStatus = BookingStatus.CONFIRMED,
            departureDate = LocalDateTime.now().plusDays(7),
            origin = "DEL",
            destination = "BOM"
        )
        
        val seatEvent = SeatUpdateEvent(
            flightScheduleId = UUID.randomUUID(),
            operation = SeatOperation.CONFIRM,
            numberOfSeats = 2,
            bookingId = UUID.randomUUID()
        )
        
        val bookingJson = "{}"
        val seatJson = "{}"
        val sendResult = mockk<SendResult<String, String>>()
        val future = CompletableFuture.completedFuture(sendResult)
        
        every { objectMapper.writeValueAsString(bookingEvent) } returns bookingJson
        every { objectMapper.writeValueAsString(seatEvent) } returns seatJson
        every { kafkaTemplate.send(any(), any(), any()) } returns future

        // When
        bookingEventPublisher.publishBookingCreated(bookingEvent)
        bookingEventPublisher.publishSeatUpdate(seatEvent)

        // Then
        verify(exactly = 1) { 
            kafkaTemplate.send("booking-events", bookingEvent.bookingId.toString(), bookingJson) 
        }
        verify(exactly = 1) { 
            kafkaTemplate.send("seat-updates", seatEvent.flightScheduleId.toString(), seatJson) 
        }
    }
}
