package com.airline.flight.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/**
 * Flight Entity - Represents a flight template/master data
 */
@Table("flights")
data class FlightEntity(
    @Id
    val id: UUID,
    
    @Column("route_id")
    val routeId: UUID,
    
    @Column("flight_number")
    val flightNumber: String,
    
    @Column("departure_time")
    val departureTime: LocalTime,
    
    @Column("arrival_time")
    val arrivalTime: LocalTime,
    
    @Column("duration_minutes")
    val durationMinutes: Int,
    
    @Column("aircraft_type")
    val aircraftType: String,
    
    @Column("total_seats")
    val totalSeats: Int,
    
    @Column("available_seats")
    val availableSeats: Int,
    
    val price: Double,

    @Column("is_active")
    val active: Boolean = true,
    
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
