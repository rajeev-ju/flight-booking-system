package com.airline.booking.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeatAvailabilityResponse(
    val available: Boolean,
    val availableSeats: Int,
    val requestedSeats: Int
)