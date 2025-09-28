package com.airline.booking.contract.response

import com.airline.booking.enums.BookingStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class BookingResponse(
    val bookingId: UUID,
    val pnr: String,
    val bookingStatus: BookingStatus,
    val message: String? = null
)
