package com.airline.booking.contract.response

import com.airline.booking.contract.request.PassengerInfo
import com.airline.booking.enums.BookingStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class BookingDetailsResponse(
    val bookingId: UUID,
    val pnr: String,
    val bookingStatus: BookingStatus,
    val flightDetails: FlightInfo,
    val passengers: List<PassengerDetails>,
    val totalAmount: BigDecimal,
    val bookingDate: LocalDateTime,
    val contactEmail: String,
    val contactPhone: String
)

data class FlightInfo(
    val flightNumber: String,
    val origin: String,
    val destination: String,
    val departureDate: LocalDateTime
)

data class PassengerDetails(
    val firstName: String,
    val lastName: String,
    val age: Int,
    val gender: String,
    val seatNumber: String?
)
