package com.airline.booking.contract.response

import com.airline.booking.enums.BookingStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class BookingSummaryResponse(
    val bookingId: UUID,
    val pnr: String,
    val flightNumber: String,
    val origin: String,
    val destination: String,
    val departureDate: LocalDateTime,
    val bookingStatus: BookingStatus,
    val totalPassengers: Int,
    val totalAmount: BigDecimal,
    val bookingDate: LocalDateTime
)
