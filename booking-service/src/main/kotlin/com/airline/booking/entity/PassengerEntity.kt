package com.airline.booking.entity

import com.airline.booking.enums.Gender
import com.airline.booking.enums.IdType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("passengers")
data class PassengerEntity(
    @Id
    val id: UUID? = null,
    
    @Column("booking_id")
    val bookingId: UUID,
    
    @Column("first_name")
    val firstName: String,
    
    @Column("last_name")
    val lastName: String,
    
    @Column("age")
    val age: Int,
    
    @Column("gender")
    val gender: Gender,
    
    @Column("id_type")
    val idType: IdType,
    
    @Column("id_number")
    val idNumber: String,
    
    @Column("seat_number")
    var seatNumber: String? = null,
    
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
