package com.airline.booking.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeatOperationRequest(
    val seats: Int
)