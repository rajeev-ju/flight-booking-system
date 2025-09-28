package com.airline.booking.event.model

import com.airline.booking.enums.BookingStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Event published when a booking is created
 */
data class BookingCreatedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val bookingId: UUID,
    val pnr: String,
    val flightScheduleId: UUID,
    val flightNumber: String,
    val numberOfSeats: Int,
    val totalAmount: BigDecimal,
    val userEmail: String,
    val bookingStatus: BookingStatus,
    val departureDate: LocalDateTime,
    val origin: String,
    val destination: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Event published when a booking is cancelled
 */
data class BookingCancelledEvent(
    val eventId: UUID = UUID.randomUUID(),
    val bookingId: UUID,
    val pnr: String,
    val flightScheduleId: UUID,
    val numberOfSeats: Int,
    val refundAmount: BigDecimal,
    val cancellationReason: String?,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

/**
 * Event for seat inventory updates
 */
data class SeatUpdateEvent(
    val eventId: UUID = UUID.randomUUID(),
    val flightScheduleId: UUID,
    val operation: SeatOperation,
    val numberOfSeats: Int,
    val bookingId: UUID?,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

enum class SeatOperation {
    RESERVE,
    RELEASE,
    CONFIRM
}

/**
 * Event published when booking status changes
 */
data class BookingStatusChangedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val bookingId: UUID,
    val pnr: String,
    val oldStatus: BookingStatus,
    val newStatus: BookingStatus,
    val reason: String?,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
