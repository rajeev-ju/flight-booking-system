package com.airline.search.controller

import com.airline.search.service.FlightSearchService
import com.airline.shared.dto.ApiResponse
import com.airline.shared.dto.FlightSearchRequest
import com.airline.shared.dto.FlightSearchResponse
import com.airline.shared.enums.SortOption
import com.airline.shared.utils.Constants
import com.airline.shared.validation.ValidationHandler
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * Clean Flight Search Controller
 */
@RestController
@RequestMapping("/api/v1/flights")
class FlightSearchController(
    private val flightSearchService: FlightSearchService,
    private val validationHandler: ValidationHandler<FlightSearchRequest>
) {
    
    private val logger = LoggerFactory.getLogger(FlightSearchController::class.java)
    
    @PostMapping("/search")
    suspend fun searchFlights(
        @Valid @RequestBody request: FlightSearchRequest,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) correlationId: String?
    ): ResponseEntity<ApiResponse<FlightSearchResponse>> {

        
        logger.info("Received flight search request: ${request.origin} -> ${request.destination}, requestId: $correlationId")

        validationHandler.validate(request)

        val searchResponse = flightSearchService.searchFlights(request)
        
        return ResponseEntity.ok(ApiResponse.success(searchResponse))
    }
    
    @GetMapping("/fastest")
    suspend fun getFastestFlights(
        @RequestParam origin: String,
        @RequestParam destination: String,
        @RequestParam departureDate: String,
        @RequestParam passengers: Int,
        @RequestParam(defaultValue = "10") maxResults: Int,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) correlationId: String?
    ): ResponseEntity<ApiResponse<FlightSearchResponse>> {

        logger.info("Received fastest flights request: $origin -> $destination, requestId: $correlationId")
        
        val request = FlightSearchRequest(
            origin = origin.uppercase(),
            destination = destination.uppercase(),
            departureDate = java.time.LocalDate.parse(departureDate),
            passengers = passengers,
            sortBy = SortOption.DURATION,
            maxResults = maxResults,
            includeConnecting = true,
            maxStops = 1
        )
        
        return searchFlights(request, correlationId)
    }
    
    @GetMapping("/cheapest")
    suspend fun getCheapestFlights(
        @RequestParam origin: String,
        @RequestParam destination: String,
        @RequestParam departureDate: String,
        @RequestParam passengers: Int,
        @RequestParam(defaultValue = "10") maxResults: Int,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) correlationId: String?
    ): ResponseEntity<ApiResponse<FlightSearchResponse>> {
        
        val actualRequestId = correlationId ?: UUID.randomUUID().toString()
        logger.info("Received cheapest flights request: $origin -> $destination, requestId: $actualRequestId")
        
        val request = FlightSearchRequest(
            origin = origin.uppercase(),
            destination = destination.uppercase(),
            departureDate = java.time.LocalDate.parse(departureDate),
            passengers = passengers,
            sortBy = SortOption.PRICE,
            maxResults = maxResults,
            includeConnecting = true,
            maxStops = 1
        )
        
        return searchFlights(request, actualRequestId)
    }
}
