package com.airline.booking.service

import com.airline.booking.cache.SeatBlockingCache
import com.airline.booking.client.FlightManagementClient
import com.airline.booking.contract.request.CreateBookingRequest
import com.airline.booking.contract.request.PassengerInfo
import com.airline.booking.dto.FlightScheduleDetails
import com.airline.booking.dto.PaymentResult
import com.airline.booking.dto.SeatAvailabilityResponse
import com.airline.booking.entity.BookingEntity
import com.airline.booking.enums.BookingStatus
import com.airline.booking.enums.Gender
import com.airline.booking.enums.IdType
import com.airline.booking.event.publisher.BookingEventPublisher
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BookingOrchestratorTest {

    private lateinit var bookingService: BookingService
    private lateinit var flightManagementClient: FlightManagementClient
    private lateinit var seatBlockingCache: SeatBlockingCache
    private lateinit var eventPublisher: BookingEventPublisher
    private lateinit var bookingOrchestrator: BookingOrchestrator
    private lateinit var pnrGeneratorService: PnrGeneratorService
    private lateinit var paymentService: PaymentService

    @BeforeEach
    fun setUp() {
        bookingService = mockk()
        flightManagementClient = mockk()
        seatBlockingCache = mockk()
        eventPublisher = mockk()
        paymentService = mockk()
        pnrGeneratorService = mockk()
        bookingOrchestrator = BookingOrchestrator(
            bookingService,
            pnrGeneratorService,
            paymentService,
            seatBlockingCache,
            flightManagementClient,
            eventPublisher
        )
    }

    @Test
    fun `should successfully create booking when seats are available and payment succeeds`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val request = createBookingRequest(flightScheduleId)
        val bookingId = UUID.randomUUID()
        val pnr = "ABC123"
        
        val flightDetails = createFlightScheduleDetails(flightScheduleId)
        val seatAvailability = SeatAvailabilityResponse(
            available = true,
            availableSeats = 50,
            requestedSeats = 2
        )
        val savedBooking = createBookingEntity(id = bookingId, pnr = pnr)
        val confirmedBooking = savedBooking.copy(bookingStatus = BookingStatus.CONFIRMED)
        
        coEvery { flightManagementClient.getFlightScheduleDetails(any()) } returns flightDetails
        coEvery { flightManagementClient.checkSeatAvailability(any(), any()) } returns seatAvailability
        coEvery { pnrGeneratorService.generatePnr() } returns pnr
        coEvery { paymentService.processPayment(any(), any(), any()) } returns PaymentResult(
            success = true,
            transactionId = "TXN123",
            message = "Payment successful"
        )
        coEvery { bookingService.saveBooking(any()) } returns savedBooking
        coEvery { bookingService.savePassengers(any()) } returns listOf()
        coEvery { bookingService.updateBookingStatus(any(), any(), any()) } returns confirmedBooking
        coEvery { seatBlockingCache.blockSeats(any(), any(), any()) } returns true
        coEvery { seatBlockingCache.confirmSeats(any()) } just Runs
        coEvery { seatBlockingCache.initializeSeatAvailability(any(), any(), any()) } just Runs
        coEvery { eventPublisher.publishBookingCreated(any()) } just Runs
        coEvery { eventPublisher.publishSeatUpdate(any()) } just Runs

        // When
        val result = bookingOrchestrator.createBooking(request)

        // Then
        assertNotNull(result)
        assertEquals(BookingStatus.CONFIRMED, result.bookingStatus)
        assertEquals(pnr, result.pnr)
        assertTrue(result.message!!.contains("successfully"))
        
        coVerify(exactly = 1) { flightManagementClient.getFlightScheduleDetails(any()) }
        coVerify(exactly = 1) { flightManagementClient.checkSeatAvailability(any(), any()) }
        coVerify(exactly = 1) { bookingService.saveBooking(any()) }
        coVerify(exactly = 1) { bookingService.savePassengers(any()) }
        coVerify(exactly = 2) { bookingService.updateBookingStatus(any(), any(), any()) } // Payment and Confirm
        coVerify(exactly = 1) { seatBlockingCache.blockSeats(any(), any(), any()) }
        coVerify(exactly = 1) { seatBlockingCache.confirmSeats(any()) }
        coVerify(exactly = 1) { eventPublisher.publishBookingCreated(any()) }
        coVerify(exactly = 1) { eventPublisher.publishSeatUpdate(any()) }
    }

    @Test
    fun `should return failed response when flight schedule details not found`() = runBlocking {
        // Given
        val request = createBookingRequest()

        coEvery { flightManagementClient.getFlightScheduleDetails(any()) } returns null

        // When + Then
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            runBlocking { bookingOrchestrator.createBooking(request) }
        }

        coVerify(exactly = 1) { flightManagementClient.getFlightScheduleDetails(any()) }
        coVerify(exactly = 0) { bookingService.saveBooking(any()) }
    }


    @Test
    fun `should handle booking failure when seats are not available`() = runBlocking {
        // Given
        val request = createBookingRequest()
        val flightScheduleId = UUID.randomUUID()
        val bookingId = UUID.randomUUID()
        val pnr = "ABC123"
        
        val flightDetails = createFlightScheduleDetails(flightScheduleId)
        val seatAvailability = SeatAvailabilityResponse(
            available = false,
            availableSeats = 1,
            requestedSeats = 2
        )
        val savedBooking = createBookingEntity(id = bookingId, pnr = pnr)
        val failedBooking = savedBooking.copy(bookingStatus = BookingStatus.FAILED)
        
        coEvery { flightManagementClient.getFlightScheduleDetails(any()) } returns flightDetails
        coEvery { flightManagementClient.checkSeatAvailability(any(), any()) } returns seatAvailability
        coEvery { pnrGeneratorService.generatePnr() } returns pnr
        coEvery { bookingService.saveBooking(any()) } returns savedBooking
        coEvery { bookingService.updateBookingStatus(any(), BookingStatus.FAILED, any()) } returns failedBooking
        coEvery { seatBlockingCache.releaseSeats(any()) } just Runs
        coEvery { eventPublisher.publishSeatUpdate(any()) } just Runs

        // When
        val result = bookingOrchestrator.createBooking(request)

        // Then
        assertEquals(BookingStatus.FAILED, result.bookingStatus)
        assertEquals(pnr, result.pnr)
        assertTrue(result.message!!.contains("Seats not available"))
        
        coVerify(exactly = 1) { flightManagementClient.checkSeatAvailability(any(), any()) }
        coVerify(exactly = 1) { bookingService.saveBooking(any()) }
        coVerify(exactly = 1) { bookingService.updateBookingStatus(any(), BookingStatus.FAILED, any()) }
        coVerify(exactly = 1) { seatBlockingCache.releaseSeats(any()) }
        coVerify(exactly = 0) { seatBlockingCache.confirmSeats(any()) }
    }

    @Test
    fun `should handle payment failure and release blocked seats`() = runBlocking {
        // Given
        val request = createBookingRequest()
        val flightScheduleId = UUID.randomUUID()
        val bookingId = UUID.randomUUID()
        val pnr = "ABC123"
        
        val flightDetails = createFlightScheduleDetails(flightScheduleId)
        val seatAvailability = SeatAvailabilityResponse(
            available = true,
            availableSeats = 50,
            requestedSeats = 2
        )
        val savedBooking = createBookingEntity(id = bookingId, pnr = pnr)
        val paymentPendingBooking = savedBooking.copy(bookingStatus = BookingStatus.PAYMENT_PENDING)
        val paymentFailedBooking = savedBooking.copy(bookingStatus = BookingStatus.PAYMENT_FAILED)
        
        coEvery { flightManagementClient.getFlightScheduleDetails(any()) } returns flightDetails
        coEvery { flightManagementClient.checkSeatAvailability(any(), any()) } returns seatAvailability
        coEvery { pnrGeneratorService.generatePnr() } returns pnr
        coEvery { paymentService.processPayment(any(), any(), any()) } returns PaymentResult(
            success = false,
            transactionId = "TXN123",
            message = "Payment failed"
        )
        coEvery { bookingService.saveBooking(any()) } returns savedBooking
        coEvery { 
            bookingService.updateBookingStatus(bookingId, BookingStatus.PAYMENT_PENDING, any()) 
        } returns paymentPendingBooking
        coEvery { 
            bookingService.updateBookingStatus(bookingId, BookingStatus.PAYMENT_CONFIRMED, any()) 
        } throws Exception("Payment gateway error")
        coEvery { 
            bookingService.updateBookingStatus(bookingId, BookingStatus.PAYMENT_FAILED, any()) 
        } returns paymentFailedBooking
        coEvery { seatBlockingCache.blockSeats(any(), any(), any()) } returns true
        coEvery { seatBlockingCache.releaseSeats(bookingId) } just Runs
        coEvery { seatBlockingCache.initializeSeatAvailability(any(), any(), any()) } just Runs

        // When
        val result = bookingOrchestrator.createBooking(request)

        // Then
        assertEquals(BookingStatus.PAYMENT_FAILED, result.bookingStatus)
        assertEquals(pnr, result.pnr)
        assertTrue(result.message!!.contains("Payment failed"))
        
        coVerify(exactly = 1) { seatBlockingCache.blockSeats(any(), any(), any()) }
        coVerify(exactly = 1) { seatBlockingCache.releaseSeats(any()) }
        coVerify(exactly = 0) { seatBlockingCache.confirmSeats(any()) }
        coVerify(exactly = 0) { bookingService.savePassengers(any()) }
    }

    @Test
    fun `should continue booking even if event publishing fails`() = runBlocking {
        // Given
        val request = createBookingRequest()
        val flightScheduleId = UUID.randomUUID()
        val bookingId = UUID.randomUUID()
        val pnr = "ABC123"
        
        val flightDetails = createFlightScheduleDetails(flightScheduleId)
        val seatAvailability = SeatAvailabilityResponse(
            available = true,
            availableSeats = 50,
            requestedSeats = 2
        )
        val savedBooking = createBookingEntity(id = bookingId, pnr = pnr)
        val confirmedBooking = savedBooking.copy(bookingStatus = BookingStatus.CONFIRMED)
        
        coEvery { flightManagementClient.getFlightScheduleDetails(any()) } returns flightDetails
        coEvery { flightManagementClient.checkSeatAvailability(any(), any()) } returns seatAvailability
        coEvery { pnrGeneratorService.generatePnr() } returns pnr
        coEvery { paymentService.processPayment(any(), any(), any()) } returns PaymentResult(
            success = true,
            transactionId = "TXN123",
            message = "Payment successful"
        )
        coEvery { bookingService.saveBooking(any()) } returns savedBooking
        coEvery { bookingService.savePassengers(any()) } returns listOf()
        coEvery { bookingService.updateBookingStatus(any(), any(), any()) } returns confirmedBooking
        coEvery { seatBlockingCache.blockSeats(any(), any(), any()) } returns true
        coEvery { seatBlockingCache.confirmSeats(any()) } just Runs
        coEvery { seatBlockingCache.initializeSeatAvailability(any(), any(), any()) } just Runs
        coEvery { eventPublisher.publishBookingCreated(any()) } throws Exception("Kafka error")
        coEvery { eventPublisher.publishSeatUpdate(any()) } throws Exception("Kafka error")

        // When
        val result = bookingOrchestrator.createBooking(request)

        // Then
        assertEquals(BookingStatus.CONFIRMED, result.bookingStatus)
        assertEquals(pnr, result.pnr)
        assertTrue(result.message!!.contains("successfully"))
        
        coVerify(exactly = 1) { eventPublisher.publishBookingCreated(any()) }
        // publishSeatUpdate won't be called because publishBookingCreated throws an exception
        coVerify(exactly = 0) { eventPublisher.publishSeatUpdate(any()) }
    }

    @Test
    fun `should generate unique PNR for each booking`() = runBlocking {
        // Given
        val request = createBookingRequest()
        val flightScheduleId = UUID.randomUUID()
        val flightDetails = createFlightScheduleDetails(flightScheduleId)
        val seatAvailability = SeatAvailabilityResponse(
            available = true,
            availableSeats = 50,
            requestedSeats = 2
        )
        
        coEvery { flightManagementClient.getFlightScheduleDetails(any()) } returns flightDetails
        coEvery { flightManagementClient.checkSeatAvailability(any(), any()) } returns seatAvailability
        coEvery { bookingService.saveBooking(any()) } answers {
            val booking = firstArg<BookingEntity>()
            booking.copy(id = UUID.randomUUID())
        }
        coEvery { pnrGeneratorService.generatePnr() } returnsMany listOf("ABC123", "XYZ789")
        coEvery { paymentService.processPayment(any(), any(), any()) } returns PaymentResult(
            success = true,
            transactionId = "TXN123",
            message = "Payment successful"
        )
        coEvery { bookingService.updateBookingStatus(any(), any(), any()) } answers {
            createBookingEntity(id = firstArg(), status = secondArg())
        }
        coEvery { seatBlockingCache.blockSeats(any(), any(), any()) } returns true
        coEvery { seatBlockingCache.confirmSeats(any()) } just Runs
        coEvery { seatBlockingCache.initializeSeatAvailability(any(), any(), any()) } just Runs
        coEvery { eventPublisher.publishBookingCreated(any()) } just Runs
        coEvery { eventPublisher.publishSeatUpdate(any()) } just Runs

        // When
        val result1 = bookingOrchestrator.createBooking(request)
        val result2 = bookingOrchestrator.createBooking(request)

        // Then
        assertNotNull(result1.pnr)
        assertNotNull(result2.pnr)
        assertTrue(result1.pnr != result2.pnr)
        assertTrue(result1.pnr.matches(Regex("[A-Z0-9]{6}")))
        assertTrue(result2.pnr.matches(Regex("[A-Z0-9]{6}")))
    }

    // Helper functions
    private fun createBookingRequest(flightScheduleId: UUID = UUID.randomUUID()): CreateBookingRequest {
        return CreateBookingRequest(
            contactEmail = "test@example.com",
            contactPhone = "1234567890",
            passengers = listOf(
                PassengerInfo(
                    firstName = "John",
                    lastName = "Doe",
                    age = 30,
                    gender = Gender.MALE,
                    idType = IdType.AADHAR,
                    idNumber = "123456789012"
                ),
                PassengerInfo(
                    firstName = "Jane",
                    lastName = "Doe",
                    age = 28,
                    gender = Gender.FEMALE,
                    idType = IdType.AADHAR,
                    idNumber = "123456789013"
                )
            ),
            flightScheduleId = flightScheduleId,
            flightNumber = "AI101",
            totalAmount = BigDecimal("4500")
        )
    }

    private fun createFlightScheduleDetails(id: UUID): FlightScheduleDetails {
        return FlightScheduleDetails(
            id = id,
            flightNumber = "AI101",
            origin = "DEL",
            destination = "BOM",
            departureDateTime = LocalDateTime.now().plusDays(7),
            arrivalDateTime = LocalDateTime.now().plusDays(7).plusHours(2),
            availableSeats = 50,
            totalSeats = 180,
            price = 5000.0,
            status = "SCHEDULED"
        )
    }

    private fun createBookingEntity(
        id: UUID? = null,
        pnr: String = "TEST123",
        status: BookingStatus = BookingStatus.INITIATED
    ): BookingEntity {
        return BookingEntity(
            id = id,
            pnr = pnr,
            flightScheduleId = UUID.randomUUID(),
            flightNumber = "AI101",
            userEmail = "test@example.com",
            userPhone = "1234567890",
            totalPassengers = 2,
            totalAmount = BigDecimal("10000"),
            bookingStatus = status,
            statusReason = null,
            bookingDate = LocalDateTime.now(),
            departureDate = LocalDateTime.now().plusDays(7),
            origin = "DEL",
            destination = "BOM"
        )
    }
}
