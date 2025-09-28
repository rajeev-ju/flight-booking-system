package com.airline.flight.controller

import com.airline.flight.contract.request.CreateScheduleRequest
import com.airline.flight.contract.request.SeatOperationRequest
import com.airline.flight.contract.response.ApiResponse
import com.airline.flight.contract.response.FlightScheduleDetails
import com.airline.flight.contract.response.ScheduleResponse
import com.airline.shared.enums.FlightStatus
import com.airline.flight.service.FlightManagementService
import com.airline.shared.model.FlightSchedule
import com.airline.shared.utils.Constants
import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.*

/**
 * Production-grade Schedule Management Controller
 */
@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = ["*"])
class ScheduleManagementController(
    private val flightService: FlightManagementService
) {
    
    private val logger = LoggerFactory.getLogger(ScheduleManagementController::class.java)

    @GetMapping
    fun getSchedulesByDate(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) correlationId: String?
    ): Flow<FlightSchedule> {
        logger.info("Fetching schedules for date: $date, correlationId: $correlationId")
        return flightService.getSchedulesForDate(date)
    }

    @GetMapping("/range")
    fun getSchedulesByDateRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) requestId: String?
    ): Flow<FlightSchedule> {
        logger.info("Fetching schedules for date range: $startDate to $endDate, requestId: $requestId")
        return flightService.getSchedulesForDateRange(startDate, endDate)
    }

    @PostMapping
    suspend fun createSchedule(
        @Valid @RequestBody request: CreateScheduleRequest,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) requestId: String?
    ): ResponseEntity<ApiResponse<ScheduleResponse>> {
        logger.info("Creating schedule for flight: ${request.flightNumber} on ${request.scheduleDate}, requestId: $requestId")

        val createdSchedule = flightService.createSchedule(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(createdSchedule, "Schedule created successfully", requestId))

    }

    @PostMapping("/{scheduleId}/reserve")
    suspend fun reserveSeats(
        @PathVariable scheduleId: UUID,
        @Valid @RequestBody request: SeatOperationRequest,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) requestId: String?
    ): ResponseEntity<ApiResponse<Any>> {

        logger.info("Reserving ${request.seats} seats for schedule: $scheduleId, requestId: $requestId")
        flightService.reserveSeats(scheduleId, request.seats)
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.toString()))

    }

    @PostMapping("/{scheduleId}/release")
    suspend fun releaseSeats(
        @PathVariable scheduleId: UUID,
        @Valid @RequestBody request: SeatOperationRequest,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) requestId: String?
    ): ResponseEntity<ApiResponse<Any>> {

        logger.info("Releasing ${request.seats} seats for schedule: $scheduleId, requestId: $requestId")
        flightService.releaseSeats(scheduleId, request.seats)
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.toString()))
    }

    @PutMapping("/{scheduleId}/status")
    suspend fun updateFlightStatus(
        @PathVariable scheduleId: UUID,
        @RequestParam status: FlightStatus,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) requestId: String?
    ): ResponseEntity<ApiResponse<Any>> {
        logger.info("Updating flight status to $status for schedule: $scheduleId, requestId: $requestId")
        flightService.updateFlightStatus(scheduleId, status)
        return ResponseEntity.ok(ApiResponse.success(data = HttpStatus.OK.toString()))

    }

    @GetMapping("/{scheduleId}/seats")
    suspend fun getAvailableSeats(
        @PathVariable scheduleId: UUID,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) correlationId: String?
    ): ResponseEntity<ApiResponse<Map<String, Any>>> {
        logger.info("Checking available seats for schedule: $scheduleId, correlationId: $correlationId")

        val availableSeats = flightService.getAvailableSeats(scheduleId)

        val seatData = mapOf(
            "scheduleId" to scheduleId.toString(),
            "availableSeats" to availableSeats
        )
        return ResponseEntity.ok(ApiResponse.success(seatData))
    }
    
    /**
     * Get flight schedule details by ID
     * This endpoint is used by the booking service to fetch flight details
     * Service layer handles the business logic and mapping
     * Exception handling is done by GlobalExceptionHandler
     */
    @GetMapping("/{scheduleId}")
    suspend fun getScheduleDetails(
        @PathVariable scheduleId: UUID,
        @RequestHeader(value = Constants.X_CORRELATION_ID, required = false) correlationId: String?
    ): ResponseEntity<FlightScheduleDetails> {
        logger.info("Fetching schedule details for ID: $scheduleId, correlationId: $correlationId")
        
        val scheduleDetails = flightService.getScheduleDetailsForBooking(scheduleId)
        return ResponseEntity.ok(scheduleDetails)
    }
}
