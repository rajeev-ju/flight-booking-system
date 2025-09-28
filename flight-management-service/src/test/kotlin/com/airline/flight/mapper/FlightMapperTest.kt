package com.airline.flight.mapper

import com.airline.flight.contract.request.CreateFlightRequest
import com.airline.flight.contract.request.UpdateFlightRequest
import com.airline.flight.entity.FlightEntity
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlightMapperTest {

    private val flightMapper = FlightMapper()

    @Test
    fun `toEntity should convert CreateFlightRequest to FlightEntity with correct values`() {
        val request = createFlightRequest()

        val result = flightMapper.toEntity(request)

        assertEquals(request.flightNumber, result.flightNumber)
        assertEquals(request.departureTime, result.departureTime)
        assertEquals(request.arrivalTime, result.arrivalTime)
        assertEquals(request.aircraftType, result.aircraftType)
        assertEquals(request.totalSeats, result.totalSeats)
        assertEquals(request.totalSeats, result.availableSeats)
        assertEquals(request.price, result.price)
        assertTrue(result.active)
        assertEquals(150, result.durationMinutes)
    }

    @Test
    fun `toEntity should calculate duration correctly when departure and arrival times span midnight`() {
        val request = CreateFlightRequest(
            flightNumber = "AI101",
            routeId = UUID.randomUUID().toString(),
            departureTime = LocalTime.of(23, 30), // 11:30 PM
            arrivalTime = LocalTime.of(2, 0), // 2:00 AM next day
            durationMinutes = 150,
            aircraftType = "Boeing 737",
            totalSeats = 180,
            price = 4500.0,
            active = true,
            effectiveFrom = LocalDateTime.now(),
            effectiveTo = LocalDateTime.now().plusYears(1)
        )

        val result = flightMapper.toEntity(request)

        assertEquals(150, result.durationMinutes) // 2.5 hours = 150 minutes
    }

    @Test
    fun `updateEntity should update existing entity with new values from UpdateFlightRequest`() {
        val existingEntity = createFlightEntity()
        val updateRequest = UpdateFlightRequest(
            flightNumber = "AI102",
            departureTime = LocalTime.of(7, 0),
            arrivalTime = LocalTime.of(9, 30),
            aircraftType = "Airbus A320",
            totalSeats = 200,
            price = 5000.0,
            active = false,
            durationMinutes = 100
        )

        val result = flightMapper.updateEntity(existingEntity, updateRequest)

        assertEquals(updateRequest.flightNumber, result.flightNumber)
        assertEquals(updateRequest.departureTime, result.departureTime)
        assertEquals(updateRequest.arrivalTime, result.arrivalTime)
        assertEquals(updateRequest.aircraftType, result.aircraftType)
        assertEquals(updateRequest.totalSeats, result.totalSeats)
        assertEquals(updateRequest.price, result.price)
        assertEquals(updateRequest.active, result.active)
        assertEquals(updateRequest.durationMinutes, result.durationMinutes)
        assertEquals(existingEntity.id, result.id)
        assertEquals(existingEntity.routeId, result.routeId)
        assertEquals(existingEntity.createdAt, result.createdAt)
    }

    @Test
    fun `toResponse should convert FlightEntity to FlightResponse with correct values`() {
        val entity = createFlightEntity()

        val result = flightMapper.toResponse(entity)

        assertEquals(entity.id.toString(), result.id)
        assertEquals(entity.flightNumber, result.flightNumber)
        assertEquals(entity.departureTime, result.departureTime)
        assertEquals(entity.arrivalTime, result.arrivalTime)
        assertEquals(entity.aircraftType, result.aircraftType)
        assertEquals(entity.totalSeats, result.totalSeats)
        assertEquals(entity.price, result.price)
        assertEquals(entity.active, result.active)
        assertEquals(entity.updatedAt, result.updatedAt)
    }

    private fun createFlightRequest() = CreateFlightRequest(
        flightNumber = "AI101",
        routeId = UUID.randomUUID().toString(),
        departureTime = LocalTime.of(6, 0),
        arrivalTime = LocalTime.of(8, 30),
        durationMinutes = 150,
        aircraftType = "Boeing 737",
        totalSeats = 180,
        price = 4500.0,
        active = true,
        effectiveFrom = LocalDateTime.now(),
        effectiveTo = LocalDateTime.now().plusYears(1)
    )

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
        active = true,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}
