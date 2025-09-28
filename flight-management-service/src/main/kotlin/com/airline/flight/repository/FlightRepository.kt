package com.airline.flight.repository

import com.airline.flight.entity.FlightEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * Repository for Flight entity operations
 */
@Repository
interface FlightRepository : CoroutineCrudRepository<FlightEntity, UUID> {

    @Query("""
        SELECT f.*, r.origin_airport_id, r.destination_airport_id 
        FROM flights f
        JOIN routes r ON f.route_id = r.id
        JOIN airports o ON r.origin_airport_id = o.id
        JOIN airports d ON r.destination_airport_id = d.id
        WHERE o.code = :origin 
        AND d.code = :destination 
        AND f.is_active = true
        ORDER BY f.departure_time
    """)
    fun findByRoute(origin: String, destination: String): Flow<FlightEntity>

    @Query("""
        SELECT * FROM flights 
        WHERE is_active = true
        ORDER BY flight_number
    """)
    fun findAllActive(): Flow<FlightEntity>

    @Query("""
        SELECT * FROM flights 
        WHERE flight_number = :flightNumber
        AND is_active = true
        LIMIT 1
    """)
    suspend fun findByFlightNumber(flightNumber: String): FlightEntity?

    @Query("""
        SELECT COUNT(*) > 0 FROM flights 
        WHERE flight_number = :flightNumber
        AND is_active = true
    """)
    suspend fun existsByFlightNumber(flightNumber: String): Boolean
}
