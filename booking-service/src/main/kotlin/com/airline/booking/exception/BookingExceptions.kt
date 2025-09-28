package com.airline.booking.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

open class BookingException(message: String, cause: Throwable? = null) : 
    RuntimeException(message, cause)

@ResponseStatus(HttpStatus.NOT_FOUND)
class BookingNotFoundException(pnr: String) : 
    BookingException("Booking not found with PNR: $pnr")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class SeatNotAvailableException(flightScheduleId: String, requestedSeats: Int) : 
    BookingException("Seats not available for flight schedule $flightScheduleId. Requested: $requestedSeats")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidBookingStateException(currentState: String, requestedAction: String) : 
    BookingException("Cannot perform $requestedAction on booking in $currentState state")

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class PaymentProcessingException(message: String) : 
    BookingException("Payment processing failed: $message")

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidPnrException(pnr: String) : 
    BookingException("Invalid PNR format: $pnr")

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class FlightServiceException(message: String) : 
    BookingException("Flight service error: $message")
