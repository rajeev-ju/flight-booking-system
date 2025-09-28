package com.airline.search.contract.response

import com.airline.search.dtos.FlightSearchResult
import com.airline.search.dtos.FlightSearchSummary

data class FlightSearchResponse(
    val searchId: String,
    val results: List<FlightSearchResult>,
    val totalResults: Int,
    val searchTimeMs: Long,
    val fromCache: Boolean = false,
    val summary: FlightSearchSummary
)
