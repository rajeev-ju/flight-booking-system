package com.airline.shared.dto

import java.time.LocalDateTime

/**
 * Flight search response DTO
 */
data class FlightSearchResponse(
    val searchId: String,
    val results: List<FlightSearchResult>,
    val totalResults: Int,
    val searchTimeMs: Long,
    val fromCache: Boolean = false,
    val summary: FlightSearchSummary
)

data class FlightSearchResult(
    val id: String,
    val flights: List<FlightSegment>,
    val totalPrice: Double,
    val totalDuration: Int, // minutes
    val type: FlightType,
    val availableSeats: Int,
    val bookingClass: String,
    val fareRules: List<String> = emptyList()
)

data class FlightSegment(
    val flightNumber: String,
    val airline: String,
    val origin: String,
    val destination: String,
    val departureDateTime: LocalDateTime,
    val arrivalDateTime: LocalDateTime,
    val duration: Int, // minutes
    val aircraft: String,
    val price: Double
)

data class FlightSearchSummary(
    val cheapestFlight: FlightSearchResult?,
    val fastestFlight: FlightSearchResult?,
    val priceRange: PriceRange,
    val durationRange: DurationRange,
    val availableAirlines: List<String>,
    val directFlightCount: Int,
    val connectingFlightCount: Int
)

data class PriceRange(
    val min: Double,
    val max: Double,
    val average: Double
)

data class DurationRange(
    val min: Int, // minutes
    val max: Int, // minutes
    val average: Int // minutes
)

enum class FlightType {
    DIRECT, CONNECTING
}
