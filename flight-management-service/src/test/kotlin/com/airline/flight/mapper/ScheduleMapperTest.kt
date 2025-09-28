package com.airline.flight.mapper

import com.airline.flight.contract.request.CreateScheduleRequest
import com.airline.flight.entity.FlightScheduleEntity
import com.airline.shared.enums.FlightStatus
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals

class ScheduleMapperTest {

    private val scheduleMapper = ScheduleMapper()

    @Test
    fun `toEntity should convert CreateScheduleRequest to FlightScheduleEntity with correct values`() {
        val request = createScheduleRequest()

        val result = scheduleMapper.toEntity(request)

        assertEquals(request.flightNumber, result.flightNumber)
        assertEquals(request.scheduleDate, result.scheduleDate)
        assertEquals(request.departureDateTime, result.departureDateTime)
        assertEquals(request.arrivalDateTime, result.arrivalDateTime)
        assertEquals(request.availableSeats, result.availableSeats)
        assertEquals(request.price, result.price)
        assertEquals(FlightStatus.SCHEDULED, result.status)
    }

    @Test
    fun `toEntity should generate new UUID for id when creating new entity`() {
        val request = createScheduleRequest()

        val result = scheduleMapper.toEntity(request)

        assertEquals(UUID::class, result.id!!::class)
    }

    @Test
    fun `toResponse should convert FlightScheduleEntity to ScheduleResponse with correct values`() {
        val entity = createScheduleEntity()

        val result = scheduleMapper.toResponse(entity)

        assertEquals(entity.flightNumber, result.flightNumber)
        assertEquals(entity.scheduleDate, result.scheduleDate)
        assertEquals(entity.departureDateTime, result.departureDateTime)
        assertEquals(entity.arrivalDateTime, result.arrivalDateTime)
        assertEquals(entity.availableSeats, result.availableSeats)
        assertEquals(entity.price, result.price)
        assertEquals(entity.status, result.status)
    }

    @Test
    fun `toSearchResponse should convert FlightScheduleEntity to ScheduleSearchResponse with provided details`() {
        val entity = createScheduleEntity()
        val airlineCode = "AI"
        val airlineName = "Air India"
        val originAirportCode = "DEL"
        val destinationAirportCode = "BOM"

        val result = scheduleMapper.toSearchResponse(
            entity = entity,
            airlineCode = airlineCode,
            airlineName = airlineName,
            originAirportCode = originAirportCode,
            destinationAirportCode = destinationAirportCode,
            aircraftType = "airbus",
            flightNumber = entity.flightNumber,
            durationMinutes = 100
        )

        assertEquals(entity.id.toString(), result.id)
        assertEquals(entity.flightNumber, result.flightNumber)
        assertEquals(airlineCode, result.airlineCode)
        assertEquals(airlineName, result.airlineName)
        assertEquals(originAirportCode, result.originAirportCode)
        assertEquals(destinationAirportCode, result.destinationAirportCode)
        assertEquals(entity.departureDateTime, result.departureDateTime)
        assertEquals(entity.arrivalDateTime, result.arrivalDateTime)
        assertEquals(entity.availableSeats, result.availableSeats)
        assertEquals(entity.price, result.price)
        assertEquals(entity.status, result.status)
    }

    @Test
    fun `toSharedModel should convert FlightScheduleEntity to shared FlightSchedule model with correct values`() {
        val entity = createScheduleEntity()

        val result = scheduleMapper.toSharedModel(entity)

        assertEquals(entity.id, result.id)
        assertEquals(entity.flightNumber, result.flightNumber)
        assertEquals(entity.scheduleDate, result.scheduleDate)
        assertEquals(entity.departureDateTime, result.departureDateTime)
        assertEquals(entity.arrivalDateTime, result.arrivalDateTime)
        assertEquals(entity.availableSeats, result.availableSeats)
        assertEquals(entity.price, result.price)
        assertEquals(FlightStatus.SCHEDULED, result.status)
    }

    @Test
    fun `toSharedModel should convert FlightStatus enum correctly to shared model enum`() {
        val entityScheduled = createScheduleEntity().copy(status = FlightStatus.SCHEDULED)
        val entityCancelled = createScheduleEntity().copy(status = FlightStatus.CANCELLED)
        val entityDelayed = createScheduleEntity().copy(status = FlightStatus.DELAYED)
        val entityBoarding = createScheduleEntity().copy(status = FlightStatus.BOARDING)
        val entityDeparted = createScheduleEntity().copy(status = FlightStatus.DEPARTED)

        val resultScheduled = scheduleMapper.toSharedModel(entityScheduled)
        val resultCancelled = scheduleMapper.toSharedModel(entityCancelled)
        val resultDelayed = scheduleMapper.toSharedModel(entityDelayed)
        val resultBoarding = scheduleMapper.toSharedModel(entityBoarding)
        val resultDeparted = scheduleMapper.toSharedModel(entityDeparted)

        assertEquals(FlightStatus.SCHEDULED, resultScheduled.status)
        assertEquals(FlightStatus.CANCELLED, resultCancelled.status)
        assertEquals(FlightStatus.DELAYED, resultDelayed.status)
        assertEquals(FlightStatus.BOARDING, resultBoarding.status)
        assertEquals(FlightStatus.DEPARTED, resultDeparted.status)
    }

    @Test
    fun `toEntity should set createdAt and updatedAt to current time`() {
        val request = createScheduleRequest()
        val beforeCreation = LocalDateTime.now().minusSeconds(5)

        val result = scheduleMapper.toEntity(request)
        val afterCreation = LocalDateTime.now().plusSeconds(5)

        assertEquals(true, result.createdAt.isAfter(beforeCreation))
        assertEquals(true, result.createdAt.isBefore(afterCreation))
        assertEquals(true, result.updatedAt.isAfter(beforeCreation))
        assertEquals(true, result.updatedAt.isBefore(afterCreation))
    }

    private fun createScheduleRequest() = CreateScheduleRequest(
        flightNumber = "AI101",
        scheduleDate = LocalDateTime.of(2025, 9, 28, 6, 0),
        departureDateTime = LocalDateTime.of(2025, 9, 28, 6, 0),
        arrivalDateTime = LocalDateTime.of(2025, 9, 28, 8, 30),
        availableSeats = 178,
        price = 4500.0
    )

    private fun createScheduleEntity() = FlightScheduleEntity(
        id = UUID.randomUUID(),
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
