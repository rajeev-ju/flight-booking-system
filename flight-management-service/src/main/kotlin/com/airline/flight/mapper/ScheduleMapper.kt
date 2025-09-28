package com.airline.flight.mapper

import com.airline.flight.contract.request.CreateScheduleRequest
import com.airline.flight.contract.response.FlightScheduleDetails
import com.airline.flight.contract.response.ScheduleResponse
import com.airline.flight.contract.response.ScheduleSearchResponse
import com.airline.flight.entity.FlightScheduleEntity
import com.airline.shared.dto.FlightScheduleWithDetails
import com.airline.shared.enums.FlightStatus
import com.airline.shared.model.FlightSchedule
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

/**
 * Mapper for FlightSchedule entity and DTOs conversion
 * Following Valhalla pattern for clean mapping logic
 */
@Component
class ScheduleMapper {

    fun toEntity(request: CreateScheduleRequest): FlightScheduleEntity {
        return FlightScheduleEntity(
            id = UUID.randomUUID(),
            flightNumber = request.flightNumber,
            scheduleDate = request.scheduleDate,
            departureDateTime = request.departureDateTime,
            arrivalDateTime = request.arrivalDateTime,
            availableSeats = request.availableSeats,
            price = request.price,
            status = FlightStatus.SCHEDULED,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    fun toResponse(entity: FlightScheduleEntity): ScheduleResponse {
        return ScheduleResponse(
            flightNumber = entity.flightNumber,
            scheduleDate = entity.scheduleDate,
            departureDateTime = entity.departureDateTime,
            arrivalDateTime = entity.arrivalDateTime,
            availableSeats = entity.availableSeats,
            price = entity.price,
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    /**
     * Convert FlightScheduleEntity to ScheduleSearchResponse with flight details
     */
    fun toSearchResponse(
        entity: FlightScheduleEntity,
        flightNumber: String,
        airlineCode: String,
        airlineName: String,
        originAirportCode: String,
        destinationAirportCode: String,
        durationMinutes: Int,
        aircraftType: String
    ): ScheduleSearchResponse {
        return ScheduleSearchResponse(
            id = entity.id.toString(),
            flightNumber = entity.flightNumber,
            airlineCode = airlineCode,
            airlineName = airlineName,
            originAirportCode = originAirportCode,
            destinationAirportCode = destinationAirportCode,
            scheduleDate = entity.scheduleDate,
            departureDateTime = entity.departureDateTime,
            arrivalDateTime = entity.arrivalDateTime,
            durationMinutes = durationMinutes,
            aircraftType = aircraftType,
            availableSeats = entity.availableSeats,
            price = entity.price,
            status = entity.status
        )
    }

    fun toSharedModel(entity: FlightScheduleEntity): FlightSchedule {
        return FlightSchedule(
            id = entity.id,
            flightNumber = entity.flightNumber,
            scheduleDate = entity.scheduleDate,
            departureDateTime = entity.departureDateTime,
            arrivalDateTime = entity.arrivalDateTime,
            availableSeats = entity.availableSeats,
            price = entity.price,
            status = FlightStatus.valueOf(entity.status.name)
        )
    }
    
    /**
     * Convert FlightScheduleWithDetails to FlightScheduleDetails response DTO
     * Used for booking service API response
     */
    fun toFlightScheduleDetails(scheduleWithDetails: FlightScheduleWithDetails): FlightScheduleDetails {
        return FlightScheduleDetails(
            id = scheduleWithDetails.id,
            flightNumber = scheduleWithDetails.flightNumber,
            origin = scheduleWithDetails.origin,
            destination = scheduleWithDetails.destination,
            departureDateTime = scheduleWithDetails.departureDateTime,
            arrivalDateTime = scheduleWithDetails.arrivalDateTime,
            availableSeats = scheduleWithDetails.availableSeats,
            totalSeats = scheduleWithDetails.totalSeats,
            price = scheduleWithDetails.price,
            status = scheduleWithDetails.status.name
        )
    }
}
