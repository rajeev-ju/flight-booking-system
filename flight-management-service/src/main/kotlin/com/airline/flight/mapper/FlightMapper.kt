package com.airline.flight.mapper

import com.airline.flight.contract.request.CreateFlightRequest
import com.airline.flight.contract.request.UpdateFlightRequest
import com.airline.flight.contract.response.FlightResponse
import com.airline.flight.contract.response.FlightSearchResponse
import com.airline.flight.entity.FlightEntity
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

/**
 * Mapper for Flight entity and DTOs conversion
 * Following Valhalla pattern for clean mapping logic
 */
@Component
class FlightMapper {
    
    /**
     * Convert CreateFlightRequest to FlightEntity
     */
    fun toEntity(request: CreateFlightRequest): FlightEntity {
        return FlightEntity(
            id = UUID.randomUUID(),
            routeId = UUID.fromString(request.routeId),
            flightNumber = request.flightNumber,
            departureTime = request.departureTime,
            arrivalTime = request.arrivalTime,
            durationMinutes = request.durationMinutes,
            aircraftType = request.aircraftType,
            totalSeats = request.totalSeats,
            availableSeats = request.totalSeats, // Initially all seats are available
            price = request.price,
            active = request.active,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Convert FlightEntity to FlightResponse
     */
    fun toResponse(entity: FlightEntity): FlightResponse {
        return FlightResponse(
            id = entity.id.toString(),
            flightNumber = entity.flightNumber,
            routeId = entity.routeId.toString(),
            departureTime = entity.departureTime,
            arrivalTime = entity.arrivalTime,
            durationMinutes = entity.durationMinutes,
            aircraftType = entity.aircraftType,
            totalSeats = entity.totalSeats,
            availableSeats = entity.availableSeats,
            price = entity.price,
            active = entity.active,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
    
    /**
     * Convert FlightEntity to FlightSearchResponse with additional route info
     */
    fun toSearchResponse(
        entity: FlightEntity,
        originAirportCode: String,
        destinationAirportCode: String,
        airlineCode: String,
        airlineName: String
    ): FlightSearchResponse {
        return FlightSearchResponse(
            id = entity.id.toString(),
            flightNumber = entity.flightNumber,
            originAirportCode = originAirportCode,
            destinationAirportCode = destinationAirportCode,
            airlineCode = airlineCode,
            airlineName = airlineName,
            departureTime = entity.departureTime,
            arrivalTime = entity.arrivalTime,
            durationMinutes = entity.durationMinutes,
            aircraftType = entity.aircraftType,
            totalSeats = entity.totalSeats,
            basePrice = entity.price,
            active = entity.active
        )
    }
    
    /**
     * Update entity with request data
     */
    fun updateEntity(entity: FlightEntity, request: UpdateFlightRequest): FlightEntity {
        return entity.copy(
            flightNumber = request.flightNumber,
            departureTime = request.departureTime,
            arrivalTime = request.arrivalTime,
            durationMinutes = request.durationMinutes,
            aircraftType = request.aircraftType,
            totalSeats = request.totalSeats,
            price = request.price,
            active = request.active,
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * Convert list of entities to list of responses
     */
    fun toResponseList(entities: List<FlightEntity>): List<FlightResponse> {
        return entities.map { toResponse(it) }
    }
}
