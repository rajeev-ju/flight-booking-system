package com.airline.booking.service

import com.airline.booking.cache.SeatBlockingCache
import com.airline.booking.client.FlightManagementClient
import com.airline.booking.contract.request.CreateBookingRequest
import com.airline.booking.contract.response.BookingResponse
import com.airline.booking.entity.BookingEntity
import com.airline.booking.entity.PassengerEntity
import com.airline.booking.enums.BookingStatus
import com.airline.booking.event.model.BookingCreatedEvent
import com.airline.booking.event.model.SeatOperation
import com.airline.booking.event.model.SeatUpdateEvent
import com.airline.booking.event.publisher.BookingEventPublisher
import com.airline.booking.exception.SeatNotAvailableException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BookingOrchestrator(
    private val bookingService: BookingService,
    private val pnrGeneratorService: PnrGeneratorService,
    private val paymentService: PaymentService,
    private val seatBlockingCache: SeatBlockingCache,
    private val flightManagementClient: FlightManagementClient,
    private val eventPublisher: BookingEventPublisher
) {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Main orchestration method for creating a booking
     */
    suspend fun createBooking(request: CreateBookingRequest): BookingResponse = coroutineScope {
        // Step 1: Fetch flight schedule details if not provided in request
        val flightDetails =
            flightManagementClient.getFlightScheduleDetails(request.flightScheduleId)
                ?: throw IllegalArgumentException("Flight schedule not found: ${request.flightScheduleId}")


        val flightNumber = flightDetails.flightNumber
        val departureDate = flightDetails.departureDateTime
        val origin = flightDetails.origin
        val destination = flightDetails.destination
        
        logger.info("Starting booking creation for flight $flightNumber")
        
        // Step 2: Generate PNR
        val pnr = pnrGeneratorService.generatePnr()
        logger.debug("Generated PNR: $pnr")
        
        // Step 3: Create initial booking entity with INITIATED status
        val bookingEntity = BookingEntity(
            pnr = pnr,
            flightScheduleId = request.flightScheduleId,
            flightNumber = flightNumber,
            userEmail = request.contactEmail,
            userPhone = request.contactPhone,
            totalPassengers = request.passengers.size,
            totalAmount = request.totalAmount,
            bookingStatus = BookingStatus.INITIATED,
            departureDate = departureDate,
            origin = origin,
            destination = destination
        )
        
        val savedBooking = bookingService.saveBooking(bookingEntity)
        val bookingId = savedBooking.id ?: throw IllegalStateException("Failed to save booking - ID is null")
        logger.info("Created booking with ID: $bookingId, PNR: $pnr")
        
        try {
            // Step 3: Check seat availability with Flight Management Service
            // We are checking after creating initiated entry because it will be used as analytical purpose of booking attempted
            val availabilityCheck = async {
                checkAndBlockSeats(savedBooking, request.passengers.size)
            }
            
            // Step 4: Prepare passenger entities
            val passengerEntities = request.passengers.mapIndexed { index, passengerInfo ->
                PassengerEntity(
                    bookingId = bookingId,
                    firstName = passengerInfo.firstName,
                    lastName = passengerInfo.lastName,
                    age = passengerInfo.age,
                    gender = passengerInfo.gender,
                    idType = passengerInfo.idType,
                    idNumber = passengerInfo.idNumber,
                    seatNumber = generateSeatNumber(index) // Auto-assign seats
                )
            }
            
            // Wait for availability check
            val seatsBlocked = availabilityCheck.await()
            
            if (!seatsBlocked) {
                throw SeatNotAvailableException(
                    savedBooking.flightScheduleId.toString(),
                    request.passengers.size
                )
            }
            
            // Step 5: Process payment (mocked - always successful)
            val paymentStatus = processPayment(savedBooking)
            
            if (!paymentStatus) {
                // Release seats if payment fails
                seatBlockingCache.releaseSeats(savedBooking.id)
                bookingService.updateBookingStatus(
                    savedBooking.id,
                    BookingStatus.PAYMENT_FAILED,
                    "Payment processing failed"
                )
                
                return@coroutineScope BookingResponse(
                    bookingId = savedBooking.id,
                    pnr = pnr,
                    bookingStatus = BookingStatus.PAYMENT_FAILED,
                    message = "Payment failed. Please try again."
                )
            }
            
            // Step 6: Save passengers
            bookingService.savePassengers(passengerEntities)
            logger.debug("Saved ${passengerEntities.size} passengers for booking ${savedBooking.id}")
            
            // Step 7: Confirm seats in cache
            seatBlockingCache.confirmSeats(savedBooking.id)
            
            // Step 8: Update booking status to CONFIRMED
            val confirmedBooking = bookingService.updateBookingStatus(
                savedBooking.id,
                BookingStatus.CONFIRMED,
                "Booking confirmed successfully"
            )
            
            // Step 9: Publish events
            publishBookingEvents(confirmedBooking)
            
            logger.info("Booking confirmed successfully. PNR: $pnr")
            
            return@coroutineScope BookingResponse(
                bookingId = confirmedBooking.id ?: bookingId,
                pnr = pnr,
                bookingStatus = BookingStatus.CONFIRMED,
                message = "Booking confirmed successfully! Your PNR is $pnr"
            )
            
        } catch (e: Exception) {
            logger.error("Booking failed for PNR: $pnr", e)
            
            // Clean up on failure
            handleBookingFailure(savedBooking, e.message ?: "Unknown error")
            
            return@coroutineScope BookingResponse(
                bookingId = bookingId,
                pnr = pnr,
                bookingStatus = BookingStatus.FAILED,
                message = "Booking failed: ${e.message}"
            )
        }
    }
    
    /**
     * Check and block seats
     */
    private suspend fun checkAndBlockSeats(
        booking: BookingEntity,
        numberOfSeats: Int
    ): Boolean {
        // First check with Flight Management Service
        val availabilityResponse = flightManagementClient.checkSeatAvailability(
            booking.flightScheduleId,
            numberOfSeats
        )
        
        if (!availabilityResponse.available) {
            logger.warn("Seats not available for flight ${booking.flightScheduleId}. " +
                    "Requested: $numberOfSeats, Available: ${availabilityResponse.availableSeats}")
            return false
        }
        
        // Initialize cache if needed
        seatBlockingCache.initializeSeatAvailability(
            booking.flightScheduleId,
            availabilityResponse.availableSeats + numberOfSeats,
            numberOfSeats
        )
        
        // Block seats in Redis
        val bookingId = booking.id ?: throw IllegalStateException("Booking ID is null")
        val blocked = seatBlockingCache.blockSeats(
            booking.flightScheduleId,
            bookingId,
            numberOfSeats
        )
        
        if (blocked) {
            logger.info("Successfully blocked $numberOfSeats seats for booking $bookingId")
        }
        
        return blocked
    }

    private suspend fun processPayment(booking: BookingEntity): Boolean {
        val bookingId = booking.id ?: throw IllegalStateException("Booking ID is null")

        val paymentResult = paymentService.processPayment(bookingId, booking.totalAmount, booking.userEmail)

        if(!paymentResult.success) {
            bookingService.updateBookingStatus(
                bookingId,
                BookingStatus.PAYMENT_FAILED,
                paymentResult.message
            )
            return false
        }
        
        // Mock payment processing - always successful for now
        // In production, this would call actual payment gateway
        
        // Update status to PAYMENT_CONFIRMED
        bookingService.updateBookingStatus(
            bookingId,
            BookingStatus.PAYMENT_CONFIRMED,
            paymentResult.message
        )
        
        return true // Mock always returns success
    }
    
    /**
     * Handle booking failure
     */
    private suspend fun handleBookingFailure(booking: BookingEntity, reason: String) {
        try {
            val bookingId = booking.id ?: return // Can't clean up without ID
            
            // Release blocked seats in cache
            seatBlockingCache.releaseSeats(bookingId)
            
            // Update booking status
            bookingService.updateBookingStatus(
                bookingId,
                BookingStatus.FAILED,
                reason
            )
            
            // Publish seat release event
            eventPublisher.publishSeatUpdate(
                SeatUpdateEvent(
                    flightScheduleId = booking.flightScheduleId,
                    operation = SeatOperation.RELEASE,
                    numberOfSeats = booking.totalPassengers,
                    bookingId = booking.id
                )
            )
        } catch (e: Exception) {
            logger.error("Error handling booking failure for ${booking.id}", e)
        }
    }

    private suspend fun publishBookingEvents(
        booking: BookingEntity
    ) {
        try {
            val bookingId = booking.id ?: throw IllegalStateException("Booking ID is null")
            
            // Publish booking created event
            eventPublisher.publishBookingCreated(
                BookingCreatedEvent(
                    bookingId = bookingId,
                    pnr = booking.pnr,
                    flightScheduleId = booking.flightScheduleId,
                    flightNumber = booking.flightNumber,
                    numberOfSeats = booking.totalPassengers,
                    totalAmount = booking.totalAmount,
                    userEmail = booking.userEmail,
                    bookingStatus = booking.bookingStatus,
                    departureDate = booking.departureDate,
                    origin = booking.origin,
                    destination = booking.destination
                )
            )
            
            // Publish seat update event
            eventPublisher.publishSeatUpdate(
                SeatUpdateEvent(
                    flightScheduleId = booking.flightScheduleId,
                    operation = SeatOperation.CONFIRM,
                    numberOfSeats = booking.totalPassengers,
                    bookingId = bookingId
                )
            )
        } catch (e: Exception) {
            logger.error("Error publishing booking events for ${booking.id}", e)
            // Don't fail the booking if event publishing fails
        }
    }
    
    /**
     * Generate seat number (simplified auto-assignment)
     */
    private fun generateSeatNumber(index: Int): String {
        val row = (index / 6) + 1
        val seat = ('A' + (index % 6))
        return "$row$seat"
    }
}
