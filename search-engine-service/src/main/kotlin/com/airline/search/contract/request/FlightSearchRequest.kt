package com.airline.search.contract.request

import com.airline.shared.enums.SortOption
import java.time.LocalDate

data class FlightSearchRequest(
    val origin: String,
    val destination: String,
    val departureDate: LocalDate,
    val passengers: Int,
    val sortBy: SortOption = SortOption.PRICE,
    val maxResults: Int = 50,
    val includeConnecting: Boolean = true,
    val maxStops: Int = 1
)
