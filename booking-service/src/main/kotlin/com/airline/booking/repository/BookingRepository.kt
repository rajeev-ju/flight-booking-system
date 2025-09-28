package com.airline.booking.repository

import com.airline.booking.entity.BookingEntity
import com.airline.booking.enums.BookingStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface BookingRepository : CoroutineCrudRepository<BookingEntity, UUID> {
    
    suspend fun findByPnr(pnr: String): BookingEntity?
    
    fun findByUserEmailOrderByBookingDateDesc(email: String): Flow<BookingEntity>

    @Query("""
        UPDATE bookings 
        SET booking_status = :status, 
            status_reason = :reason,
            updated_at = :updatedAt
        WHERE id = :bookingId
        RETURNING *
    """)
    suspend fun updateBookingStatus(
        bookingId: UUID,
        status: BookingStatus,
        reason: String?,
        updatedAt: LocalDateTime = LocalDateTime.now()
    ): BookingEntity?
}
