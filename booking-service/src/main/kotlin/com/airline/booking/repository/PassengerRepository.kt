package com.airline.booking.repository

import com.airline.booking.entity.PassengerEntity
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PassengerRepository : CoroutineCrudRepository<PassengerEntity, UUID> {
    
    fun findByBookingId(bookingId: UUID): Flow<PassengerEntity>
    
    suspend fun deleteByBookingId(bookingId: UUID): Long
    
    @Query("""
        UPDATE passengers 
        SET seat_number = :seatNumber 
        WHERE id = :passengerId
        RETURNING *
    """)
    suspend fun updateSeatNumber(passengerId: UUID, seatNumber: String): PassengerEntity?
    
    @Query("""
        SELECT COUNT(*) FROM passengers 
        WHERE booking_id = :bookingId
    """)
    suspend fun countByBookingId(bookingId: UUID): Long
}
