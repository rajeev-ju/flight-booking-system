package com.airline.search.dtos

data class FlightSegment(
    val flightId: Long,
    val scheduleId: Long,
    val flightNumber: String,
    val airline: String,
    val origin: AirportInfo,
    val destination: AirportInfo,
    val departure: FlightTime,
    val arrival: FlightTime,
    val duration: Int,
    val price: Double,
    val aircraft: String
)
