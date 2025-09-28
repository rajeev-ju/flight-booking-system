package com.airline.flight.repository

import com.airline.flight.entity.FlightScheduleEntity
import com.airline.shared.enums.FlightStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Data class for complete flight schedule details from join query
 * Maps to the result of findScheduleWithCompleteDetails query
 */
data class FlightScheduleCompleteDetails(
    val id: UUID,
    val flightNumber: String,
    val departureDateTime: LocalDateTime,
    val arrivalDateTime: LocalDateTime,
    val availableSeats: Int,
    val price: Double,
    val status: FlightStatus,
    val totalSeats: Int,
    val originCity: String,
    val originCode: String,
    val destinationCity: String,
    val destinationCode: String,
    val distanceKm: Int?,
    val airlineName: String,
    val airlineCode: String
)

/**
 * Repository for FlightSchedule entity operations
 */
@Repository
interface FlightScheduleEntityRepository : CoroutineCrudRepository<FlightScheduleEntity, UUID> {
    
    /**
     * Find available flights for a specific route and date
     */
    @Query("""
        SELECT fs.* FROM flight_schedules fs
        JOIN flights f ON fs.flight_id = f.id
        JOIN routes r ON f.route_id = r.id
        JOIN airports o ON r.origin_airport_id = o.id
        JOIN airports d ON r.destination_airport_id = d.id
        WHERE o.code = :origin 
        AND d.code = :destination
        AND DATE(fs.schedule_date) = :date
        AND fs.available_seats >= :minSeats
        AND fs.status = 'SCHEDULED'
        ORDER BY fs.departure_date_time
    """)
    fun findAvailableFlights(
        origin: String, 
        destination: String, 
        date: LocalDate, 
        minSeats: Int
    ): Flow<FlightScheduleEntity>
    
    /**
     * Find schedule by flight and date
     */
    @Query("""
        SELECT * FROM flight_schedules
        WHERE flight_id = :flightId
        AND DATE(schedule_date) = :date
        AND status = 'SCHEDULED'
        ORDER BY departure_date_time
    """)
    suspend fun findByFlightAndDate(flightId: UUID, date: LocalDate): FlightScheduleEntity?
    
    /**
     * Find schedules by date
     */
    @Query("""
        SELECT * FROM flight_schedules
        WHERE DATE(schedule_date) = :date
        AND status = 'SCHEDULED'
        ORDER BY departure_date_time
    """)
    fun findByDate(date: LocalDate): Flow<FlightScheduleEntity>
    
    /**
     * Find schedules by date range
     */
    @Query("""
        SELECT * FROM flight_schedules
        WHERE DATE(schedule_date) BETWEEN :startDate AND :endDate
        AND status = 'SCHEDULED'
        ORDER BY schedule_date, departure_date_time
    """)
    fun findByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<FlightScheduleEntity>
    
    /**
     * Find schedules by flight ID
     */
    @Query("""
        SELECT * FROM flight_schedules
        WHERE flight_id = :flightId
        ORDER BY schedule_date, departure_date_time
    """)
    fun findByFlightId(flightId: UUID): Flow<FlightScheduleEntity>
    
    /**
     * Reserve seats for booking
     */
    @Modifying
    @Query("""
        UPDATE flight_schedules 
        SET available_seats = available_seats - :seats,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :scheduleId 
        AND available_seats >= :seats
        AND status = 'SCHEDULED'
    """)
    suspend fun reserveSeats(scheduleId: UUID, seats: Int): Int
    
    /**
     * Release seats (e.g., booking cancelled)
     */
    @Modifying
    @Query("""
        UPDATE flight_schedules 
        SET available_seats = available_seats + :seats,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :scheduleId
        AND status = 'SCHEDULED'
    """)
    suspend fun releaseSeats(scheduleId: UUID, seats: Int): Int
    
    /**
     * Update flight status
     */
    @Modifying
    @Query("""
        UPDATE flight_schedules 
        SET status = :status,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = :scheduleId
    """)
    suspend fun updateStatus(scheduleId: UUID, status: FlightStatus): Int
    
    /**
     * Check seat availability
     */
    @Query("""
        SELECT available_seats FROM flight_schedules
        WHERE id = :scheduleId
        AND status = 'SCHEDULED'
    """)
    suspend fun getAvailableSeats(scheduleId: UUID): Int
    
    /**
     * Find schedule with complete details including flight, route, and airport information
     * Optimized single query with joins to fetch all required data
     */
    @Query("""
        SELECT 
            fs.id,
            fs.flight_number,
            fs.departure_date_time,
            fs.arrival_date_time,
            fs.available_seats,
            fs.price,
            fs.status,
            f.total_seats,
            origin.city as origin_city,
            origin.code as origin_code,
            dest.city as destination_city,
            dest.code as destination_code,
            r.distance_km,
            al.name as airline_name,
            al.code as airline_code
        FROM flight_schedules fs
        INNER JOIN flights f ON fs.flight_number = f.flight_number
        INNER JOIN routes r ON f.route_id = r.id
        INNER JOIN airports origin ON r.origin_airport_id = origin.id
        INNER JOIN airports dest ON r.destination_airport_id = dest.id
        INNER JOIN airlines al ON r.airline_id = al.id
        WHERE fs.id = :scheduleId
    """)
    suspend fun findScheduleWithCompleteDetails(scheduleId: UUID): FlightScheduleCompleteDetails?
}
