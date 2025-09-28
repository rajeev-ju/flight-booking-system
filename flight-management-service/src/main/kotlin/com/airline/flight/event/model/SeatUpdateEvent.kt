package com.airline.flight.event.model

import java.time.LocalDateTime
import java.util.*

/**
 * Event for seat inventory updates
 * This event is published by the booking service and consumed by flight management service
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
