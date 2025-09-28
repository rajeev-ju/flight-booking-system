package com.airline.flight.repository

import com.airline.flight.entity.FlightEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for FlightRepository using mocks
 * Tests repository interface methods without database
 */
class FlightRepositorySimpleTest {

    private lateinit var flightRepository: FlightRepository
    
    @BeforeEach
    fun setup() {
        flightRepository = mockk()
    }

    @Test
    fun `save should persist flight entity and return saved entity`() = runTest {
        // Given
        val flightEntity = createFlightEntity()
        val savedEntity = flightEntity.copy()
        
        coEvery { flightRepository.save(flightEntity) } returns savedEntity

        // When
        val result = flightRepository.save(flightEntity)

        // Then
        assertNotNull(result)
        assertEquals(savedEntity.id, result.id)
        assertEquals(savedEntity.flightNumber, result.flightNumber)
        assertEquals(savedEntity.routeId, result.routeId)
        assertEquals(savedEntity.durationMinutes, result.durationMinutes)
        
        coVerify(exactly = 1) { flightRepository.save(flightEntity) }
    }

    @Test
    fun `findById should return flight when exists`() = runTest {
        // Given
        val flightEntity = createFlightEntity()
        
        coEvery { flightRepository.findById(flightEntity.id) } returns flightEntity

        // When
        val foundEntity = flightRepository.findById(flightEntity.id)

        // Then
        assertNotNull(foundEntity)
        assertEquals(flightEntity.id, foundEntity.id)
        assertEquals(flightEntity.flightNumber, foundEntity.flightNumber)
        
        coVerify(exactly = 1) { flightRepository.findById(flightEntity.id) }
    }

    @Test
    fun `findById should return null when flight does not exist`() = runTest {
        // Given
        val nonExistentId = UUID.randomUUID()
        
        coEvery { flightRepository.findById(nonExistentId) } returns null

        // When
        val foundEntity = flightRepository.findById(nonExistentId)

        // Then
        assertNull(foundEntity)
        
        coVerify(exactly = 1) { flightRepository.findById(nonExistentId) }
    }

    @Test
    fun `findByFlightNumber should return flight when exists and is active`() = runTest {
        // Given
        val flightEntity = createFlightEntity(flightNumber = "AI101", active = true)
        
        coEvery { flightRepository.findByFlightNumber("AI101") } returns flightEntity

        // When
        val foundEntity = flightRepository.findByFlightNumber("AI101")

        // Then
        assertNotNull(foundEntity)
        assertEquals("AI101", foundEntity.flightNumber)
        assertTrue(foundEntity.active)
        
        coVerify(exactly = 1) { flightRepository.findByFlightNumber("AI101") }
    }

    @Test
    fun `findAllActive should return only active flights`() = runTest {
        // Given
        val activeFlight1 = createFlightEntity(flightNumber = "AI101", active = true)
        val activeFlight2 = createFlightEntity(flightNumber = "AI102", active = true)
        
        coEvery { flightRepository.findAllActive() } returns flowOf(activeFlight1, activeFlight2)

        // When
        val activeFlights = flightRepository.findAllActive().toList()

        // Then
        assertEquals(2, activeFlights.size)
        assertTrue(activeFlights.all { it.active })
        assertTrue(activeFlights.any { it.flightNumber == "AI101" })
        assertTrue(activeFlights.any { it.flightNumber == "AI102" })
        
        coVerify(exactly = 1) { flightRepository.findAllActive() }
    }

    @Test
    fun `delete should remove flight from database`() = runTest {
        // Given
        val flightEntity = createFlightEntity()
        
        coEvery { flightRepository.delete(flightEntity) } returns Unit
        coEvery { flightRepository.findById(flightEntity.id) } returns null

        // When
        flightRepository.delete(flightEntity)
        val foundEntity = flightRepository.findById(flightEntity.id)

        // Then
        assertNull(foundEntity)
        
        coVerify(exactly = 1) { flightRepository.delete(flightEntity) }
        coVerify(exactly = 1) { flightRepository.findById(flightEntity.id) }
    }

    @Test
    fun `existsByFlightNumber should return true when active flight exists`() = runTest {
        // Given
        coEvery { flightRepository.existsByFlightNumber("AI101") } returns true

        // When
        val exists = flightRepository.existsByFlightNumber("AI101")

        // Then
        assertTrue(exists)
        
        coVerify(exactly = 1) { flightRepository.existsByFlightNumber("AI101") }
    }

    @Test
    fun `count should return correct number of flights`() = runTest {
        // Given
        coEvery { flightRepository.count() } returns 3

        // When
        val count = flightRepository.count()

        // Then
        assertEquals(3, count)
        
        coVerify(exactly = 1) { flightRepository.count() }
    }

    private fun createFlightEntity(
        id: UUID = UUID.randomUUID(),
        routeId: UUID = UUID.randomUUID(),
        flightNumber: String = "AI101",
        departureTime: LocalTime = LocalTime.of(6, 0),
        arrivalTime: LocalTime = LocalTime.of(8, 30),
        durationMinutes: Int = 150,
        aircraftType: String = "Boeing 737",
        totalSeats: Int = 180,
        availableSeats: Int = 180,
        price: Double = 5000.0,
        active: Boolean = true
    ) = FlightEntity(
        id = id,
        routeId = routeId,
        flightNumber = flightNumber,
        departureTime = departureTime,
        arrivalTime = arrivalTime,
        durationMinutes = durationMinutes,
        aircraftType = aircraftType,
        totalSeats = totalSeats,
        availableSeats = availableSeats,
        price = price,
        active = active
    )
}
