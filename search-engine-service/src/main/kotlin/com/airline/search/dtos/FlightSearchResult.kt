package com.airline.search.dtos

import com.airline.shared.enums.FlightType


data class FlightSearchResult(
    val id: String,
    val type: FlightType, // DIRECT or CONNECTING
    val flights: List<FlightSegment>,
    val totalPrice: Double,
    val totalDuration: Int,
    val availableSeats: Int,
    val departure: FlightTime,
    val arrival: FlightTime
)
