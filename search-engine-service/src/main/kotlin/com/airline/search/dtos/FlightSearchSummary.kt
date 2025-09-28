package com.airline.search.dtos

data class FlightSearchSummary(
    val cheapestFlight: FlightSearchResult?,
    val fastestFlight: FlightSearchResult?,
    val priceRange: PriceRange,
    val durationRange: DurationRange,
    val availableAirlines: List<String>,
    val directFlightCount: Int,
    val connectingFlightCount: Int
)
