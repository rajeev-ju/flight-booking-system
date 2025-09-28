package com.airline.flight.service

import com.airline.flight.contract.request.CreateFlightRequest
import com.airline.flight.contract.request.CreateScheduleRequest
import com.airline.flight.contract.request.UpdateFlightRequest
import com.airline.flight.contract.response.FlightResponse
import com.airline.flight.contract.response.ScheduleResponse
import com.airline.flight.entity.FlightEntity
import com.airline.flight.entity.FlightScheduleEntity
import com.airline.shared.enums.FlightStatus
import com.airline.shared.exception.FlightNotFoundException
import com.airline.flight.mapper.FlightMapper
import com.airline.flight.mapper.ScheduleMapper
import com.airline.flight.repository.FlightRepository
import com.airline.flight.repository.FlightScheduleEntityRepository
import com.airline.shared.model.Flight
import com.airline.shared.model.FlightSchedule
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlightManagementServiceTest {

    private val flightRepository = mockk<FlightRepository>()
    private val scheduleRepository = mockk<FlightScheduleEntityRepository>()
    private val flightMapper = mockk<FlightMapper>()
    private val scheduleMapper = mockk<ScheduleMapper>()

    private val flightManagementService = FlightManagementService(
        flightRepository,
        scheduleRepository,
        flightMapper,
        scheduleMapper
    )

    @Test
    fun `getAllActiveFlights should return all active flights when flights exist`() = runTest {
        val flightEntity = createFlightEntity()
        val flight = createFlight(flightEntity.id)
        
        every { flightRepository.findAllActive() } returns flowOf(flightEntity)

        val result = flightManagementService.getAllActiveFlights().toList()

        assertEquals(1, result.size)
        assertEquals(flight.id, result[0].id)
        verify { flightRepository.findAllActive() }
    }

    @Test
    fun `getAllActiveFlights should return empty flow when no active flights exist`() = runTest {
        every { flightRepository.findAllActive() } returns flowOf()

        val result = flightManagementService.getAllActiveFlights().toList()

        assertTrue(result.isEmpty())
        verify { flightRepository.findAllActive() }
    }

    @Test
    fun `createFlight should successfully create flight when valid request provided`() = runTest {
        val request = createFlightRequest()
        val flightEntity = createFlightEntity()
        val savedEntity = createFlightEntity().copy(id = UUID.randomUUID())
        val expectedResponse = createFlightResponse()

        every { flightMapper.toEntity(request) } returns flightEntity
        coEvery { flightRepository.findByFlightNumber(request.flightNumber) } returns null
        coEvery { flightRepository.save(flightEntity) } returns savedEntity
        every { flightMapper.toResponse(savedEntity) } returns expectedResponse

        val result = flightManagementService.createFlight(request)

        assertEquals(expectedResponse, result)
        verify { flightMapper.toEntity(request) }
        coVerify { flightRepository.findByFlightNumber(request.flightNumber) }
        coVerify { flightRepository.save(flightEntity) }
        verify { flightMapper.toResponse(savedEntity) }
    }

    @Test
    fun `updateFlight should successfully update flight when flight exists`() = runTest {
        val flightId = UUID.randomUUID()
        val request = createUpdateFlightRequest()
        val existingEntity = createFlightEntity().copy(id = flightId)
        val updatedEntity = createFlightEntity().copy(id = flightId, flightNumber = "AI102")
        val savedEntity = updatedEntity.copy()
        val expectedResponse = createFlightResponse().copy(flightNumber = "AI102")

        coEvery { flightRepository.findById(flightId) } returns existingEntity
        every { flightMapper.updateEntity(existingEntity, request) } returns updatedEntity
        coEvery { flightRepository.save(updatedEntity) } returns savedEntity
        every { flightMapper.toResponse(savedEntity) } returns expectedResponse

        val result = flightManagementService.updateFlight(flightId, request)

        assertEquals(expectedResponse, result)
        coVerify { flightRepository.findById(flightId) }
        verify { flightMapper.updateEntity(existingEntity, request) }
        coVerify { flightRepository.save(updatedEntity) }
        verify { flightMapper.toResponse(savedEntity) }
    }

    @Test
    fun `updateFlight should throw FlightNotFoundException when flight does not exist`() = runTest {
        val flightId = UUID.randomUUID()
        val request = createUpdateFlightRequest()

        coEvery { flightRepository.findById(flightId) } returns null

        val exception = assertThrows<FlightNotFoundException> {
            flightManagementService.updateFlight(flightId, request)
        }

        assertEquals("Flight with id: $flightId doesn't exist", exception.message)
        coVerify { flightRepository.findById(flightId) }
    }

    @Test
    fun `getFlightById should return flight when flight exists`() = runTest {
        val flightId = UUID.randomUUID()
        val flightEntity = createFlightEntity().copy(id = flightId)
        val expectedResponse = createFlightResponse()

        coEvery { flightRepository.findById(flightId) } returns flightEntity
        every { flightMapper.toResponse(flightEntity) } returns expectedResponse

        val result = flightManagementService.getFlightById(flightId)

        assertEquals(expectedResponse, result)
        coVerify { flightRepository.findById(flightId) }
        verify { flightMapper.toResponse(flightEntity) }
    }

    @Test
    fun `getFlightById should throw FlightNotFoundException when flight does not exist`() = runTest {
        val flightId = UUID.randomUUID()

        coEvery { flightRepository.findById(flightId) } returns null

        val exception = assertThrows<FlightNotFoundException> {
            flightManagementService.getFlightById(flightId)
        }

        assertEquals("Flight with id: $flightId doesn't exist", exception.message)
        coVerify { flightRepository.findById(flightId) }
    }

    @Test
    fun `getFlightsByRoute should return flights when flights exist for route`() = runTest {
        val origin = "DEL"
        val destination = "BOM"
        val flightEntity = createFlightEntity()
        val expectedResponse = createFlightResponse()

        every { flightRepository.findByRoute(origin, destination) } returns flowOf(flightEntity)
        every { flightMapper.toResponse(flightEntity) } returns expectedResponse

        val result = flightManagementService.getFlightsByRoute(origin, destination).toList()

        assertEquals(1, result.size)
        assertEquals(expectedResponse, result[0])
        verify { flightRepository.findByRoute(origin, destination) }
        verify { flightMapper.toResponse(flightEntity) }
    }

    @Test
    fun `getSchedulesForDate should return schedules when schedules exist for date`() = runTest {
        val date = LocalDate.of(2025, 9, 28)
        val scheduleEntity = createScheduleEntity()
        val expectedSchedule = createFlightSchedule()

        every { scheduleRepository.findByDate(date) } returns flowOf(scheduleEntity)
        every { scheduleMapper.toSharedModel(scheduleEntity) } returns expectedSchedule

        val result = flightManagementService.getSchedulesForDate(date).toList()

        assertEquals(1, result.size)
        assertEquals(expectedSchedule, result[0])
        verify { scheduleRepository.findByDate(date) }
        verify { scheduleMapper.toSharedModel(scheduleEntity) }
    }

    @Test
    fun `getSchedulesForDateRange should return schedules when schedules exist for date range`() = runTest {
        val startDate = LocalDate.of(2025, 9, 28)
        val endDate = LocalDate.of(2025, 9, 30)
        val scheduleEntity = createScheduleEntity()
        val expectedSchedule = createFlightSchedule()

        every { scheduleRepository.findByDateRange(startDate, endDate) } returns flowOf(scheduleEntity)
        every { scheduleMapper.toSharedModel(scheduleEntity) } returns expectedSchedule

        val result = flightManagementService.getSchedulesForDateRange(startDate, endDate).toList()

        assertEquals(1, result.size)
        assertEquals(expectedSchedule, result[0])
        verify { scheduleRepository.findByDateRange(startDate, endDate) }
        verify { scheduleMapper.toSharedModel(scheduleEntity) }
    }

    @Test
    fun `createSchedule should successfully create schedule when valid request provided`() = runTest {
        val request = createScheduleRequest()
        val scheduleEntity = createScheduleEntity()
        val savedEntity = createScheduleEntity().copy(id = UUID.randomUUID())
        val expectedResponse = createScheduleResponse()

        every { scheduleMapper.toEntity(request) } returns scheduleEntity
        coEvery { scheduleRepository.save(scheduleEntity) } returns savedEntity
        every { scheduleMapper.toResponse(savedEntity) } returns expectedResponse

        val result = flightManagementService.createSchedule(request)

        assertEquals(expectedResponse, result)
        verify { scheduleMapper.toEntity(request) }
        coVerify { scheduleRepository.save(scheduleEntity) }
        verify { scheduleMapper.toResponse(savedEntity) }
    }

    @Test
    fun `getAvailableFlights should return available flights when flights exist for route and date`() = runTest {
        val origin = "DEL"
        val destination = "BOM"
        val date = LocalDate.of(2025, 9, 28)
        val minSeats = 2
        val scheduleEntity = createScheduleEntity()
        val expectedSchedule = createFlightSchedule()

        every { scheduleRepository.findAvailableFlights(origin, destination, date, minSeats) } returns flowOf(scheduleEntity)
        every { scheduleMapper.toSharedModel(scheduleEntity) } returns expectedSchedule

        val result = flightManagementService.getAvailableFlights(origin, destination, date, minSeats).toList()

        assertEquals(1, result.size)
        assertEquals(expectedSchedule, result[0])
        verify { scheduleRepository.findAvailableFlights(origin, destination, date, minSeats) }
        verify { scheduleMapper.toSharedModel(scheduleEntity) }
    }

    @Test
    fun `reserveSeats should return true when seats successfully reserved`() = runTest {
        val scheduleId = UUID.randomUUID()
        val seats = 2

        coEvery { scheduleRepository.reserveSeats(scheduleId, seats) } returns 1

        val result = flightManagementService.reserveSeats(scheduleId, seats)

        assertTrue(result)
        coVerify { scheduleRepository.reserveSeats(scheduleId, seats) }
    }

    @Test
    fun `reserveSeats should return false when seats reservation fails`() = runTest {
        val scheduleId = UUID.randomUUID()
        val seats = 2

        coEvery { scheduleRepository.reserveSeats(scheduleId, seats) } returns 0

        val result = flightManagementService.reserveSeats(scheduleId, seats)

        assertEquals(false, result)
        coVerify { scheduleRepository.reserveSeats(scheduleId, seats) }
    }

    @Test
    fun `releaseSeats should return true when seats successfully released`() = runTest {
        val scheduleId = UUID.randomUUID()
        val seats = 2

        coEvery { scheduleRepository.releaseSeats(scheduleId, seats) } returns 1

        val result = flightManagementService.releaseSeats(scheduleId, seats)

        assertTrue(result)
        coVerify { scheduleRepository.releaseSeats(scheduleId, seats) }
    }

    @Test
    fun `updateFlightStatus should return true when status successfully updated`() = runTest {
        val scheduleId = UUID.randomUUID()
        val status = FlightStatus.CANCELLED

        coEvery { scheduleRepository.updateStatus(scheduleId, status) } returns 1

        val result = flightManagementService.updateFlightStatus(scheduleId, status)

        assertTrue(result)
        coVerify { scheduleRepository.updateStatus(scheduleId, status) }
    }

    @Test
    fun `getAvailableSeats should return seat count when schedule exists`() = runTest {
        val scheduleId = UUID.randomUUID()
        val expectedSeats = 150

        coEvery { scheduleRepository.getAvailableSeats(scheduleId) } returns expectedSeats

        val result = flightManagementService.getAvailableSeats(scheduleId)

        assertEquals(expectedSeats, result)
        coVerify { scheduleRepository.getAvailableSeats(scheduleId) }
    }

    private fun createFlightEntity() = FlightEntity(
        id = UUID.randomUUID(),
        routeId = UUID.randomUUID(),
        flightNumber = "AI101",
        departureTime = LocalTime.of(6, 0),
        arrivalTime = LocalTime.of(8, 30),
        durationMinutes = 150,
        aircraftType = "Boeing 737",
        totalSeats = 180,
        availableSeats = 178,
        price = 4500.0,
        active = true
    )

    private fun createFlight(id: UUID) = Flight(
        id = id,
        flightNumber = "AI101",
        airlineCode = "AI",
        originAirportCode = "DEL",
        destinationAirportCode = "BOM",
        departureTime = LocalTime.of(6, 0),
        arrivalTime = LocalTime.of(8, 30),
        duration = 150,
        aircraft = "Boeing 737",
        totalSeats = 180,
        basePrice = 4500.0,
        active = true,
        effectiveFrom = LocalDateTime.now(),
        effectiveTo = LocalDateTime.now().plusYears(1)
    )

    private fun createFlightRequest() = CreateFlightRequest(
        flightNumber = "AI101",
        departureTime = LocalTime.of(6, 0),
        arrivalTime = LocalTime.of(8, 30),
        aircraftType = "Boeing 737",
        totalSeats = 180,
        price = 4500.0,
        routeId = "route-1",
        durationMinutes = 100,
        active = true,
        effectiveFrom = LocalDateTime.now(),
        effectiveTo = LocalDateTime.now().plusYears(1)
    )

    private fun createUpdateFlightRequest() = UpdateFlightRequest(
        flightNumber = "AI102",
        departureTime = LocalTime.of(7, 0),
        arrivalTime = LocalTime.of(9, 30),
        aircraftType = "Boeing 737",
        totalSeats = 180,
        price = 4800.0,
        active = true,
        durationMinutes = 100
    )

    private fun createFlightResponse() = FlightResponse(
        id = "1",
        flightNumber = "AI101",
        departureTime = LocalTime.of(6, 0),
        arrivalTime = LocalTime.of(8, 30),
        aircraftType = "Boeing 737",
        totalSeats = 180,
        price = 4500.0,
        active = true,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
        routeId = "route-1",
        durationMinutes = 100,
        availableSeats = 200
    )

    private fun createScheduleEntity() = FlightScheduleEntity(
        id = UUID.randomUUID(),
        flightNumber = "AI101",
        scheduleDate = LocalDateTime.of(2025, 9, 28, 6, 0),
        departureDateTime = LocalDateTime.of(2025, 9, 28, 6, 0),
        arrivalDateTime = LocalDateTime.of(2025, 9, 28, 8, 30),
        availableSeats = 178,
        price = 4500.0,
        status = FlightStatus.SCHEDULED
    )

    private fun createFlightSchedule() = FlightSchedule(
        id = UUID.randomUUID(),
        flightNumber = "AI101",
        scheduleDate = LocalDateTime.of(2025, 9, 28, 6, 0),
        departureDateTime = LocalDateTime.of(2025, 9, 28, 6, 0),
        arrivalDateTime = LocalDateTime.of(2025, 9, 28, 8, 30),
        availableSeats = 178,
        price = 4500.0,
        status = FlightStatus.SCHEDULED
    )

    private fun createScheduleRequest() = CreateScheduleRequest(
        flightNumber = "AI101",
        scheduleDate = LocalDateTime.of(2025, 9, 28, 6, 0),
        departureDateTime = LocalDateTime.of(2025, 9, 28, 6, 0),
        arrivalDateTime = LocalDateTime.of(2025, 9, 28, 8, 30),
        availableSeats = 178,
        price = 4500.0
    )

    private fun createScheduleResponse() = ScheduleResponse(
        flightNumber = "AI101",
        scheduleDate = LocalDateTime.of(2025, 9, 28, 6, 0),
        departureDateTime = LocalDateTime.of(2025, 9, 28, 6, 0),
        arrivalDateTime = LocalDateTime.of(2025, 9, 28, 8, 30),
        availableSeats = 178,
        price = 4500.0,
        status = FlightStatus.SCHEDULED,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}
