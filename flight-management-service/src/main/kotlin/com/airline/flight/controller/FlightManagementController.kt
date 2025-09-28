package com.airline.flight.controller

import com.airline.flight.contract.request.CreateFlightRequest
import com.airline.flight.contract.request.UpdateFlightRequest
import com.airline.flight.contract.response.ApiResponse
import com.airline.flight.contract.response.FlightResponse
import com.airline.flight.service.FlightManagementService
import com.airline.shared.model.Flight
import com.airline.shared.model.FlightSchedule
import com.airline.shared.utils.Constants
import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = ["*"])
class FlightManagementController(
    private val flightService: FlightManagementService
) {

    private val logger = LoggerFactory.getLogger(FlightManagementController::class.java)

    @GetMapping
    fun getAllFlights(
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) correlationId: String?
    ): Flow<Flight> {
        logger.info("Fetching all active flights, correlationId: $correlationId")
        return flightService.getAllActiveFlights()
    }

    @GetMapping("/{flightId}")
    suspend fun getFlightById(
        @PathVariable flightId: UUID,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) correlationId: String?
    ): ResponseEntity<ApiResponse<FlightResponse>> {
        logger.info("Fetching flight by ID: $flightId, correlationId: $correlationId")

        val flight = flightService.getFlightById(flightId)
        return ResponseEntity.ok(ApiResponse.success(flight))
    }

    @GetMapping("/route")
    fun getFlightsByRoute(
        @RequestParam origin: String,
        @RequestParam destination: String,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) correlationId: String?
    ): Flow<FlightResponse> {
        logger.info("Fetching flights for route: $origin -> $destination, correlationId: $correlationId")
        return flightService.getFlightsByRoute(origin.uppercase(), destination.uppercase())
    }

    @PostMapping
    suspend fun createFlight(
        @Valid @RequestBody request: CreateFlightRequest,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) correlationId: String?
    ): ResponseEntity<ApiResponse<FlightResponse>> {

        logger.info("Creating new flight: ${request.flightNumber}, correlationId: $correlationId")
        val createdFlight = flightService.createFlight(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdFlight))
    }

    @PutMapping("/{flightId}")
    suspend fun updateFlight(
        @PathVariable flightId: UUID,
        @Valid @RequestBody request: UpdateFlightRequest,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) correlationId: String?
    ): ResponseEntity<ApiResponse<FlightResponse>> {

        logger.info("Updating flight: $flightId, correlationId: $correlationId")
        val updatedFlight = flightService.updateFlight(flightId, request)
        return ResponseEntity.ok(ApiResponse.success(updatedFlight, "Flight updated successfully", correlationId))
    }

    @GetMapping("/available")
    fun getAvailableFlights(
        @RequestParam origin: String,
        @RequestParam destination: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam(defaultValue = "1") minSeats: Int,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) correlationId: String?
    ): Flow<FlightSchedule> {
        logger.info("Fetching available flights: $origin -> $destination on $date, minSeats: $minSeats, correlationId: $correlationId")
        return flightService.getAvailableFlights(
            origin.uppercase(),
            destination.uppercase(),
            date,
            minSeats
        )
    }
}
