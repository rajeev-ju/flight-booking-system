package com.airline.flight.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * Route Entity - Represents a flight route between two airports
 */
@Table("routes")
data class RouteEntity(
    @Id
    val id: UUID? = null,
    
    @Column("origin_airport_id")
    val originAirportId: UUID,
    
    @Column("destination_airport_id")
    val destinationAirportId: UUID,
    
    @Column("airline_id")
    val airlineId: UUID,
    
    @Column("distance_km")
    val distanceKm: Int?,
    
    @Column("base_price")
    val basePrice: Double,
    
    @Column("is_active")
    val active: Boolean = true,
    
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
