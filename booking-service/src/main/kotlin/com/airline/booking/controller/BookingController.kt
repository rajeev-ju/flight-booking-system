package com.airline.booking.controller

import com.airline.booking.contract.request.CreateBookingRequest
import com.airline.booking.contract.response.BookingDetailsResponse
import com.airline.booking.contract.response.BookingResponse
import com.airline.booking.contract.response.BookingSummaryResponse
import com.airline.booking.service.BookingOrchestrator
import com.airline.booking.service.BookingService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bookings")
class BookingController(
    private val bookingOrchestrator: BookingOrchestrator,
    private val bookingService: BookingService
) {
    
    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping
    suspend fun createBooking(
        @Valid @RequestBody request: CreateBookingRequest
    ): ResponseEntity<BookingResponse> {
        logger.info("Received booking request for schedule ${request.flightScheduleId} with ${request.passengers.size} passengers")
        
        val response = bookingOrchestrator.createBooking(request)
        
        return ResponseEntity
            .status(if (response.bookingStatus.name.contains("CONFIRMED")) HttpStatus.CREATED else HttpStatus.BAD_REQUEST)
            .body(response)
    }
    
    /**
     * Get booking details by PNR
     */
    @GetMapping("/{pnr}")
    suspend fun getBookingByPnr(
        @PathVariable pnr: String
    ): ResponseEntity<BookingDetailsResponse> {
        logger.info("Fetching booking details for PNR: $pnr")
        
        val booking = bookingService.getBookingByPnr(pnr)
        
        return ResponseEntity.ok(booking)
    }

    @GetMapping("/user/{email}")
    suspend fun getUserBookings(
        @PathVariable email: String
    ): ResponseEntity<List<BookingSummaryResponse>> {
        logger.info("Fetching bookings for user: $email")
        
        val bookings = bookingService.getBookingsByUserEmail(email)
        
        return ResponseEntity.ok(bookings)
    }

    @PutMapping("/{pnr}/cancel")
    suspend fun cancelBooking(
        @PathVariable pnr: String,
        @RequestParam(required = false) reason: String?
    ): ResponseEntity<BookingResponse> {
        logger.info("Cancelling booking with PNR: $pnr")
        
        val cancelledBooking = bookingService.cancelBooking(pnr, reason)
        
        val bookingId = cancelledBooking.id ?: throw IllegalStateException("Cancelled booking has no ID")
        
        val response = BookingResponse(
            bookingId = bookingId,
            pnr = cancelledBooking.pnr,
            bookingStatus = cancelledBooking.bookingStatus,
            message = "Booking cancelled successfully"
        )
        
        return ResponseEntity.ok(response)
    }
}
