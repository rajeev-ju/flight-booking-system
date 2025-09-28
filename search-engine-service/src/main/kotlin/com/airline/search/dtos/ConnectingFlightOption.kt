package com.airline.search.dtos

data class ConnectingFlightOption(
    val id: String,
    val segments: List<FlightOption>,
    val layoverAirport: String,
    val layoverDuration: Int, // minutes
    val totalDuration: Int, // minutes
    val totalPrice: Double,
    val minAvailableSeats: Int
)
