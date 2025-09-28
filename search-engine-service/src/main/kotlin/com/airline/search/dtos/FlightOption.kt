package com.airline.search.dtos

import java.util.*

data class FlightOption(
    val scheduleId: UUID,
    val flightNumber: String,
    val airline: String,
    val departureTime: String, // ISO format
    val arrivalTime: String, // ISO format
    val duration: Int, // minutes
    val price: Double,
    val availableSeats: Int,
    val aircraft: String
)