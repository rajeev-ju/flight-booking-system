package com.airline.booking.service

import com.airline.booking.contract.response.BookingSummaryResponse
import com.airline.booking.entity.BookingEntity
import com.airline.booking.entity.PassengerEntity
import com.airline.booking.enums.BookingStatus
import com.airline.booking.enums.Gender
import com.airline.booking.enums.IdType
import com.airline.booking.exception.BookingNotFoundException
import com.airline.booking.exception.InvalidBookingStateException
import com.airline.booking.repository.BookingRepository
import com.airline.booking.repository.PassengerRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BookingServiceTest {

    private lateinit var bookingRepository: BookingRepository
    private lateinit var passengerRepository: PassengerRepository
    private lateinit var bookingService: BookingService

    @BeforeEach
    fun setUp() {
        bookingRepository = mockk()
        passengerRepository = mockk()
        bookingService = BookingService(
            bookingRepository,
            passengerRepository
        )
    }

    @Test
    fun `should successfully save a new booking with null id and return saved booking with generated id`() = runBlocking {
        // Given
        val booking = createBookingEntity(id = null)
        val savedBooking = booking.copy(id = UUID.randomUUID())
        
        coEvery { bookingRepository.save(booking) } returns savedBooking

        // When
        val result = bookingService.saveBooking(booking)

        // Then
        assertNotNull(result.id)
        assertEquals(savedBooking.pnr, result.pnr)
        assertEquals(savedBooking.flightScheduleId, result.flightScheduleId)
        coVerify(exactly = 1) { bookingRepository.save(booking) }
    }

    @Test
    fun `should successfully save multiple passengers for a booking`() = runBlocking {
        // Given
        val bookingId = UUID.randomUUID()
        val passengers = listOf(
            createPassengerEntity(bookingId = bookingId, firstName = "John"),
            createPassengerEntity(bookingId = bookingId, firstName = "Jane")
        )
        
        coEvery { passengerRepository.saveAll(passengers) } returns flowOf(*passengers.toTypedArray())

        // When
        val result = bookingService.savePassengers(passengers)

        // Then
        assertEquals(2, result.size)
        assertEquals("John", result[0].firstName)
        assertEquals("Jane", result[1].firstName)
        coVerify(exactly = 1) { passengerRepository.saveAll(passengers) }
    }

    @Test
    fun `should update booking status from INITIATED to CONFIRMED with reason`() = runBlocking {
        // Given
        val bookingId = UUID.randomUUID()
        val oldStatus = BookingStatus.INITIATED
        val newStatus = BookingStatus.CONFIRMED
        val reason = "Payment successful"
        val existingBooking = createBookingEntity(id = bookingId, status = oldStatus)
        val updatedBooking = existingBooking.copy(bookingStatus = newStatus)
        
        coEvery { bookingRepository.findById(bookingId) } returns existingBooking
        coEvery { 
            bookingRepository.updateBookingStatus(bookingId, newStatus, reason, any()) 
        } returns updatedBooking

        // When
        val result = bookingService.updateBookingStatus(bookingId, newStatus, reason)

        // Then
        assertEquals(newStatus, result.bookingStatus)
        assertEquals(bookingId, result.id)
        coVerify(exactly = 1) { bookingRepository.updateBookingStatus(bookingId, newStatus, reason, any()) }
    }

    @Test
    fun `should throw BookingNotFoundException when updating status for non-existent booking`(): Unit = runBlocking {
        // Given
        val bookingId = UUID.randomUUID()
        
        coEvery { bookingRepository.findById(bookingId) } returns null
        coEvery { 
            bookingRepository.updateBookingStatus(any(), any(), any(), any()) 
        } returns null

        // When & Then
        assertThrows<BookingNotFoundException> {
            bookingService.updateBookingStatus(bookingId, BookingStatus.CONFIRMED, "Test")
        }
    }

    @Test
    fun `should successfully retrieve booking by PNR with passengers`() = runBlocking {
        // Given
        val pnr = "ABC123"
        val bookingId = UUID.randomUUID()
        val booking = createBookingEntity(id = bookingId, pnr = pnr)
        val passengers = listOf(
            createPassengerEntity(bookingId = bookingId, firstName = "John"),
            createPassengerEntity(bookingId = bookingId, firstName = "Jane")
        )
        
        coEvery { bookingRepository.findByPnr(pnr) } returns booking
        coEvery { passengerRepository.findByBookingId(bookingId) } returns flowOf(*passengers.toTypedArray())

        // When
        val result = bookingService.getBookingByPnr(pnr)

        // Then
        assertNotNull(result)
        assertEquals(pnr, result.pnr)
        assertEquals(2, result.passengers.size)
        assertEquals("John", result.passengers[0].firstName)
        coVerify(exactly = 1) { bookingRepository.findByPnr(pnr) }
        coVerify(exactly = 1) { passengerRepository.findByBookingId(bookingId) }
    }

    @Test
    fun `should throw BookingNotFoundException when retrieving non-existent booking by PNR`() = runBlocking {
        // Given
        val pnr = "INVALID"
        
        coEvery { bookingRepository.findByPnr(pnr) } returns null

        // When & Then
        assertThrows<BookingNotFoundException> {
            bookingService.getBookingByPnr(pnr)
        }
        coVerify(exactly = 1) { bookingRepository.findByPnr(pnr) }
        coVerify(exactly = 0) { passengerRepository.findByBookingId(any()) }
    }

    @Test
    fun `should return list of booking summaries for user email sorted by booking date`() = runBlocking {
        // Given
        val email = "user@example.com"
        val bookings = listOf(
            createBookingEntity(
                id = UUID.randomUUID(),
                userEmail = email,
                bookingDate = LocalDateTime.now().minusDays(1)
            ),
            createBookingEntity(
                id = UUID.randomUUID(),
                userEmail = email,
                bookingDate = LocalDateTime.now()
            )
        )
        
        coEvery { 
            bookingRepository.findByUserEmailOrderByBookingDateDesc(email) 
        } returns flowOf(*bookings.toTypedArray())

        // When
        val result = bookingService.getBookingsByUserEmail(email)

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it is BookingSummaryResponse })
        coVerify(exactly = 1) { bookingRepository.findByUserEmailOrderByBookingDateDesc(email) }
    }

    @Test
    fun `should successfully cancel a booking in CONFIRMED status`() = runBlocking {
        // Given
        val pnr = "ABC123"
        val reason = "Customer requested cancellation"
        val bookingId = UUID.randomUUID()
        val booking = createBookingEntity(
            id = bookingId,
            pnr = pnr,
            status = BookingStatus.CONFIRMED
        )
        val cancelledBooking = booking.copy(bookingStatus = BookingStatus.CANCELLED)
        
        coEvery { bookingRepository.findByPnr(pnr) } returns booking
        coEvery { bookingRepository.findById(bookingId) } returns booking
        coEvery { 
            bookingRepository.updateBookingStatus(
                bookingId, 
                BookingStatus.CANCELLED, 
                reason, 
                any()
            ) 
        } returns cancelledBooking

        // When
        val result = bookingService.cancelBooking(pnr, reason)

        // Then
        assertEquals(BookingStatus.CANCELLED, result.bookingStatus)
        assertEquals(pnr, result.pnr)
        coVerify(exactly = 1) { bookingRepository.findByPnr(pnr) }
        coVerify(exactly = 1) { 
            bookingRepository.updateBookingStatus(bookingId, BookingStatus.CANCELLED, reason, any()) 
        }
    }

    @Test
    fun `should throw InvalidBookingStateException when cancelling already cancelled booking`() = runBlocking {
        // Given
        val pnr = "ABC123"
        val booking = createBookingEntity(
            id = UUID.randomUUID(),
            pnr = pnr,
            status = BookingStatus.CANCELLED
        )
        
        coEvery { bookingRepository.findByPnr(pnr) } returns booking

        // When & Then
        assertThrows<InvalidBookingStateException> {
            bookingService.cancelBooking(pnr, "Test cancellation")
        }
        coVerify(exactly = 1) { bookingRepository.findByPnr(pnr) }
        coVerify(exactly = 0) { bookingRepository.updateBookingStatus(any(), any(), any(), any()) }
    }

    @Test
    fun `should throw InvalidBookingStateException when cancelling failed booking`(): Unit = runBlocking {
        // Given
        val pnr = "ABC123"
        val booking = createBookingEntity(
            id = UUID.randomUUID(),
            pnr = pnr,
            status = BookingStatus.FAILED
        )
        
        coEvery { bookingRepository.findByPnr(pnr) } returns booking

        // When & Then
        assertThrows<InvalidBookingStateException> {
            bookingService.cancelBooking(pnr, "Test cancellation")
        }
    }

    @Test
    fun `should return empty list when no bookings exist for user email`() = runBlocking {
        // Given
        val email = "noBookings@example.com"
        
        coEvery { 
            bookingRepository.findByUserEmailOrderByBookingDateDesc(email) 
        } returns flowOf()

        // When
        val result = bookingService.getBookingsByUserEmail(email)

        // Then
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { bookingRepository.findByUserEmailOrderByBookingDateDesc(email) }
    }

    // Helper functions
    private fun createBookingEntity(
        id: UUID? = null,
        pnr: String = "TEST123",
        status: BookingStatus = BookingStatus.INITIATED,
        userEmail: String = "test@example.com",
        bookingDate: LocalDateTime = LocalDateTime.now()
    ): BookingEntity {
        return BookingEntity(
            id = id,
            pnr = pnr,
            flightScheduleId = UUID.randomUUID(),
            flightNumber = "AI101",
            userEmail = userEmail,
            userPhone = "1234567890",
            totalPassengers = 2,
            totalAmount = BigDecimal("10000"),
            bookingStatus = status,
            statusReason = null,
            bookingDate = bookingDate,
            departureDate = LocalDateTime.now().plusDays(7),
            origin = "DEL",
            destination = "BOM"
        )
    }

    private fun createPassengerEntity(
        id: UUID? = null,
        bookingId: UUID,
        firstName: String = "Test",
        lastName: String = "User"
    ): PassengerEntity {
        return PassengerEntity(
            id = id,
            bookingId = bookingId,
            firstName = firstName,
            lastName = lastName,
            age = 30,
            gender = Gender.MALE,
            idType = IdType.AADHAR,
            idNumber = "123456789012",
            seatNumber = "12A"
        )
    }
}
