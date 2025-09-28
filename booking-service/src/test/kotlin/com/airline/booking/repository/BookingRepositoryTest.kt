package com.airline.booking.repository

import com.airline.booking.entity.BookingEntity
import com.airline.booking.enums.BookingStatus
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BookingRepositoryTest {

    private lateinit var bookingRepository: BookingRepository

    @BeforeEach
    fun setUp() {
        bookingRepository = mockk()
    }

    @Test
    fun `should save new booking with null id and return booking with generated id`() = runBlocking {
        // Given
        val bookingToSave = createBookingEntity(id = null)
        val savedBooking = bookingToSave.copy(id = UUID.randomUUID())
        
        coEvery { bookingRepository.save(bookingToSave) } returns savedBooking

        // When
        val result = bookingRepository.save(bookingToSave)

        // Then
        assertNotNull(result.id)
        assertEquals(savedBooking.pnr, result.pnr)
        assertEquals(savedBooking.flightScheduleId, result.flightScheduleId)
        coVerify(exactly = 1) { bookingRepository.save(bookingToSave) }
    }

    @Test
    fun `should find booking by PNR when it exists`() = runBlocking {
        // Given
        val pnr = "ABC123"
        val booking = createBookingEntity(pnr = pnr)
        
        coEvery { bookingRepository.findByPnr(pnr) } returns booking

        // When
        val result = bookingRepository.findByPnr(pnr)

        // Then
        assertNotNull(result)
        assertEquals(pnr, result.pnr)
        coVerify(exactly = 1) { bookingRepository.findByPnr(pnr) }
    }

    @Test
    fun `should return null when finding booking by non-existent PNR`() = runBlocking {
        // Given
        val pnr = "INVALID"
        
        coEvery { bookingRepository.findByPnr(pnr) } returns null

        // When
        val result = bookingRepository.findByPnr(pnr)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { bookingRepository.findByPnr(pnr) }
    }

    @Test
    fun `should find bookings by user email ordered by booking date descending`() = runBlocking {
        // Given
        val email = "user@example.com"
        val oldBooking = createBookingEntity(
            id = UUID.randomUUID(),
            userEmail = email,
            bookingDate = LocalDateTime.now().minusDays(2)
        )
        val recentBooking = createBookingEntity(
            id = UUID.randomUUID(),
            userEmail = email,
            bookingDate = LocalDateTime.now()
        )
        
        coEvery { 
            bookingRepository.findByUserEmailOrderByBookingDateDesc(email) 
        } returns flowOf(recentBooking, oldBooking)

        // When
        val result = bookingRepository.findByUserEmailOrderByBookingDateDesc(email).toList()

        // Then
        assertEquals(2, result.size)
        assertEquals(recentBooking.id, result[0].id)
        assertEquals(oldBooking.id, result[1].id)
        assertTrue(result[0].bookingDate.isAfter(result[1].bookingDate))
        coVerify(exactly = 1) { bookingRepository.findByUserEmailOrderByBookingDateDesc(email) }
    }

    @Test
    fun `should return empty flow when no bookings exist for user email`() = runBlocking {
        // Given
        val email = "noBookings@example.com"
        
        coEvery { 
            bookingRepository.findByUserEmailOrderByBookingDateDesc(email) 
        } returns flowOf()

        // When
        val result = bookingRepository.findByUserEmailOrderByBookingDateDesc(email).toList()

        // Then
        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { bookingRepository.findByUserEmailOrderByBookingDateDesc(email) }
    }

    @Test
    fun `should update booking status successfully and return updated booking`() = runBlocking {
        // Given
        val bookingId = UUID.randomUUID()
        val newStatus = BookingStatus.CONFIRMED
        val reason = "Payment successful"
        val updatedAt = LocalDateTime.now()
        val updatedBooking = createBookingEntity(
            id = bookingId,
            status = newStatus
        )
        
        coEvery { 
            bookingRepository.updateBookingStatus(bookingId, newStatus, reason, any()) 
        } returns updatedBooking

        // When
        val result = bookingRepository.updateBookingStatus(bookingId, newStatus, reason, updatedAt)

        // Then
        assertNotNull(result)
        assertEquals(bookingId, result.id)
        assertEquals(newStatus, result.bookingStatus)
        coVerify(exactly = 1) { 
            bookingRepository.updateBookingStatus(bookingId, newStatus, reason, any()) 
        }
    }

    @Test
    fun `should return null when updating non-existent booking status`() = runBlocking {
        // Given
        val bookingId = UUID.randomUUID()
        val newStatus = BookingStatus.CONFIRMED
        val reason = "Payment successful"
        
        coEvery { 
            bookingRepository.updateBookingStatus(bookingId, newStatus, reason, any()) 
        } returns null

        // When
        val result = bookingRepository.updateBookingStatus(bookingId, newStatus, reason)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { 
            bookingRepository.updateBookingStatus(bookingId, newStatus, reason, any()) 
        }
    }

    @Test
    fun `should find booking by id when it exists`() = runBlocking {
        // Given
        val bookingId = UUID.randomUUID()
        val booking = createBookingEntity(id = bookingId)
        
        coEvery { bookingRepository.findById(bookingId) } returns booking

        // When
        val result = bookingRepository.findById(bookingId)

        // Then
        assertNotNull(result)
        assertEquals(bookingId, result.id)
        coVerify(exactly = 1) { bookingRepository.findById(bookingId) }
    }

    @Test
    fun `should return null when finding booking by non-existent id`() = runBlocking {
        // Given
        val bookingId = UUID.randomUUID()
        
        coEvery { bookingRepository.findById(bookingId) } returns null

        // When
        val result = bookingRepository.findById(bookingId)

        // Then
        assertNull(result)
        coVerify(exactly = 1) { bookingRepository.findById(bookingId) }
    }

    // Helper function
    private fun createBookingEntity(
        id: UUID? = UUID.randomUUID(),
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
}
