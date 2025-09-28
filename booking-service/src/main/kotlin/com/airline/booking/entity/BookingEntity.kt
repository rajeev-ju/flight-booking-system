package com.airline.booking.entity

import com.airline.booking.enums.BookingStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Table("bookings")
data class BookingEntity(
    @Id
    val id: UUID? = null,
    
    @Column("pnr")
    val pnr: String,
    
    @Column("flight_schedule_id")
    val flightScheduleId: UUID,
    
    @Column("flight_number")
    val flightNumber: String,
    
    @Column("user_email")
    val userEmail: String,
    
    @Column("user_phone")
    val userPhone: String,
    
    @Column("total_passengers")
    val totalPassengers: Int,
    
    @Column("total_amount")
    val totalAmount: BigDecimal,
    
    @Column("booking_status")
    var bookingStatus: BookingStatus,
    
    @Column("status_reason")
    var statusReason: String? = null,
    
    @Column("booking_date")
    val bookingDate: LocalDateTime = LocalDateTime.now(),
    
    @Column("departure_date")
    val departureDate: LocalDateTime,
    
    @Column("origin")
    val origin: String,
    
    @Column("destination")
    val destination: String,
    
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column("updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
