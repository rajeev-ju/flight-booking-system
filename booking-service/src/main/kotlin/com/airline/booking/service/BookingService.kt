package com.airline.booking.service

import com.airline.booking.contract.response.BookingDetailsResponse
import com.airline.booking.contract.response.BookingSummaryResponse
import com.airline.booking.contract.response.FlightInfo
import com.airline.booking.contract.response.PassengerDetails
import com.airline.booking.entity.BookingEntity
import com.airline.booking.entity.PassengerEntity
import com.airline.booking.enums.BookingStatus
import com.airline.booking.exception.BookingNotFoundException
import com.airline.booking.exception.InvalidBookingStateException
import com.airline.booking.repository.BookingRepository
import com.airline.booking.repository.PassengerRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class BookingService(
    private val bookingRepository: BookingRepository,
    private val passengerRepository: PassengerRepository
) {
    
    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun saveBooking(booking: BookingEntity): BookingEntity {
        val savedBooking = bookingRepository.save(booking)
        logger.info("Saved booking with PNR: ${savedBooking.pnr}")
        return savedBooking
    }

    suspend fun updateBookingStatus(
        bookingId: UUID,
        newStatus: BookingStatus,
        reason: String? = null
    ): BookingEntity {
        val booking = bookingRepository.findById(bookingId)
            ?: throw BookingNotFoundException("Booking not found: $bookingId")
        
        val oldStatus = booking.bookingStatus
        
        // Update booking
        val updatedBooking = bookingRepository.updateBookingStatus(
            bookingId = bookingId,
            status = newStatus,
            reason = reason
        ) ?: throw BookingNotFoundException("Failed to update booking: $bookingId")
        
        logger.info("Updated booking $bookingId status from $oldStatus to $newStatus")
        return updatedBooking
    }

    suspend fun getBookingByPnr(pnr: String): BookingDetailsResponse {
        val booking = bookingRepository.findByPnr(pnr)
            ?: throw BookingNotFoundException(pnr)
        
        val bookingId = booking.id ?: throw IllegalStateException("Booking ID is null for PNR: $pnr")
        
        val passengers = passengerRepository.findByBookingId(bookingId)
            .map { passenger ->
                PassengerDetails(
                    firstName = passenger.firstName,
                    lastName = passenger.lastName,
                    age = passenger.age,
                    gender = passenger.gender.name,
                    seatNumber = passenger.seatNumber
                )
            }
            .toList()
        
        return BookingDetailsResponse(
            bookingId = bookingId,
            pnr = booking.pnr,
            bookingStatus = booking.bookingStatus,
            flightDetails = FlightInfo(
                flightNumber = booking.flightNumber,
                origin = booking.origin,
                destination = booking.destination,
                departureDate = booking.departureDate
            ),
            passengers = passengers,
            totalAmount = booking.totalAmount,
            bookingDate = booking.bookingDate,
            contactEmail = booking.userEmail,
            contactPhone = booking.userPhone
        )
    }

    suspend fun getBookingsByUserEmail(email: String): List<BookingSummaryResponse> {
        return bookingRepository.findByUserEmailOrderByBookingDateDesc(email)
            .map { booking ->
                BookingSummaryResponse(
                    bookingId = booking.id ?: throw IllegalStateException("Booking ID is null"),
                    pnr = booking.pnr,
                    flightNumber = booking.flightNumber,
                    origin = booking.origin,
                    destination = booking.destination,
                    departureDate = booking.departureDate,
                    bookingStatus = booking.bookingStatus,
                    totalPassengers = booking.totalPassengers,
                    totalAmount = booking.totalAmount,
                    bookingDate = booking.bookingDate
                )
            }
            .toList()
    }

    suspend fun cancelBooking(pnr: String, reason: String? = null): BookingEntity {
        val booking = bookingRepository.findByPnr(pnr)
            ?: throw BookingNotFoundException(pnr)
        
        // Validate current status
        if (booking.bookingStatus in listOf(BookingStatus.CANCELLED, BookingStatus.FAILED)) {
            throw InvalidBookingStateException(
                booking.bookingStatus.name,
                "cancel"
            )
        }
        
        // Check if cancellation is allowed (e.g., 2 hours before departure)
        val hoursBeforeDeparture = java.time.Duration.between(
            LocalDateTime.now(),
            booking.departureDate
        ).toHours()
        
        if (hoursBeforeDeparture < 2) {
            throw InvalidBookingStateException(
                "Less than 2 hours before departure",
                "cancel"
            )
        }
        
        val bookingId = booking.id ?: throw IllegalStateException("Booking ID is null for PNR: $pnr")
        
        return updateBookingStatus(
            bookingId = bookingId,
            newStatus = BookingStatus.CANCELLED,
            reason = reason ?: "User requested cancellation"
        )
    }

    suspend fun savePassengers(passengers: List<PassengerEntity>): List<PassengerEntity> {
        return passengerRepository.saveAll(passengers).toList()
    }
}
