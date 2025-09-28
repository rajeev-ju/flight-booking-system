package com.airline.flight.util

import com.airline.flight.contract.request.SeatOperationRequest
import com.airline.flight.contract.response.FlightResponse
import com.airline.flight.contract.response.ScheduleResponse
import com.airline.flight.entity.FlightEntity
import com.airline.flight.entity.FlightScheduleEntity
import com.airline.shared.enums.FlightStatus
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 */
object TestDataBuilder {

    // Flight Entity Builders
    fun createFlightEntity(
        id: UUID? = null,
        flightNumber: String = "AI101",
        departureTime: LocalTime = LocalTime.of(6, 0),
        arrivalTime: LocalTime = LocalTime.of(8, 30),
        aircraftType: String = "Boeing 737",
        totalSeats: Int = 180,
        availableSeats: Int = 178,
        price: Double = 4500.0,
        active: Boolean = true
    ) = FlightEntity(
        id = id ?: UUID.randomUUID(),
        routeId = UUID.randomUUID(),
        flightNumber = flightNumber,
        departureTime = departureTime,
        arrivalTime = arrivalTime,
        durationMinutes = calculateDurationMinutes(departureTime, arrivalTime),
        aircraftType = aircraftType,
        totalSeats = totalSeats,
        availableSeats = availableSeats,
        price = price,
        active = active,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    // Schedule Entity Builders
    fun createScheduleEntity(
        id: UUID? = null,
        flightNumber: String = "AI101",
        scheduleDate: LocalDateTime = LocalDateTime.of(2025, 9, 28, 6, 0),
        departureDateTime: LocalDateTime = LocalDateTime.of(2025, 9, 28, 6, 0),
        arrivalDateTime: LocalDateTime = LocalDateTime.of(2025, 9, 28, 8, 30),
        availableSeats: Int = 178,
        price: Double = 4500.0,
        status: FlightStatus = FlightStatus.SCHEDULED
    ) = FlightScheduleEntity(
        id = id ?: UUID.randomUUID(),
        flightNumber = flightNumber,
        scheduleDate = scheduleDate,
        departureDateTime = departureDateTime,
        arrivalDateTime = arrivalDateTime,
        availableSeats = availableSeats,
        price = price,
        status = status,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )


    fun createSeatOperationRequest(
        seats: Int = 2
    ) = SeatOperationRequest(seats = seats)

    // Response Builders
    fun createFlightResponse(
        id: String = "1",
        flightNumber: String = "AI101",
        departureTime: LocalTime = LocalTime.of(6, 0),
        arrivalTime: LocalTime = LocalTime.of(8, 30),
        aircraftType: String = "Boeing 737",
        totalSeats: Int = 180,
        price: Double = 4500.0,
        active: Boolean = true
    ) = FlightResponse(
        id = id,
        flightNumber = flightNumber,
        departureTime = departureTime,
        arrivalTime = arrivalTime,
        aircraftType = aircraftType,
        totalSeats = totalSeats,
        price = price,
        active = active,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
        routeId = "route-1",
        durationMinutes = 100,
        availableSeats = 200
    )

    fun createScheduleResponse(
        flightNumber: String = "AI101",
        scheduleDate: LocalDateTime = LocalDateTime.of(2025, 9, 28, 6, 0),
        departureDateTime: LocalDateTime = LocalDateTime.of(2025, 9, 28, 6, 0),
        arrivalDateTime: LocalDateTime = LocalDateTime.of(2025, 9, 28, 8, 30),
        availableSeats: Int = 178,
        price: Double = 4500.0,
        status: FlightStatus = FlightStatus.SCHEDULED
    ) = ScheduleResponse(
        flightNumber = flightNumber,
        scheduleDate = scheduleDate,
        departureDateTime = departureDateTime,
        arrivalDateTime = arrivalDateTime,
        availableSeats = availableSeats,
        price = price,
        status = status,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    // Utility Methods
    private fun calculateDurationMinutes(departureTime: LocalTime, arrivalTime: LocalTime): Int {
        val departure = departureTime.toSecondOfDay()
        val arrival = arrivalTime.toSecondOfDay()
        
        val durationSeconds = if (arrival >= departure) {
            arrival - departure
        } else {
            // Handle overnight flights
            (24 * 3600) - departure + arrival
        }
        
        return (durationSeconds / 60).toInt()
    }

    class ScheduleEntityBuilder {
        private var id: UUID? = null
        private var flightNumber: String = "AI101"
        private var scheduleDate: LocalDateTime = LocalDateTime.of(2025, 9, 28, 6, 0)
        private var departureDateTime: LocalDateTime = LocalDateTime.of(2025, 9, 28, 6, 0)
        private var arrivalDateTime: LocalDateTime = LocalDateTime.of(2025, 9, 28, 8, 30)
        private var availableSeats: Int = 178
        private var price: Double = 4500.0
        private var status: FlightStatus = FlightStatus.SCHEDULED

        fun withId(id: UUID?) = apply { this.id = id }
        fun withFlightNumber(flightNumber: String) = apply { this.flightNumber = flightNumber }
        fun withScheduleDate(scheduleDate: LocalDateTime) = apply { this.scheduleDate = scheduleDate }
        fun withDepartureDateTime(departureDateTime: LocalDateTime) = apply { this.departureDateTime = departureDateTime }
        fun withArrivalDateTime(arrivalDateTime: LocalDateTime) = apply { this.arrivalDateTime = arrivalDateTime }
        fun withAvailableSeats(availableSeats: Int) = apply { this.availableSeats = availableSeats }
        fun withPrice(price: Double) = apply { this.price = price }
        fun withStatus(status: FlightStatus) = apply { this.status = status }

        fun build() = createScheduleEntity(
            id, flightNumber, scheduleDate, departureDateTime, 
            arrivalDateTime, availableSeats, price, status
        )
    }

    fun scheduleEntity() = ScheduleEntityBuilder()
}
