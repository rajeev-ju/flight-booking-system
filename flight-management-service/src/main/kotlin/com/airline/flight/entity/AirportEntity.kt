package com.airline.flight.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * Airport Entity - Represents an airport
 */
@Table("airports")
data class AirportEntity(
    @Id
    val id: UUID? = null,
    
    val code: String, // 3-letter IATA code
    
    val name: String,
    
    val city: String,
    
    val country: String = "India",
    
    val timezone: String,
    
    @Column("is_active")
    val active: Boolean = true,
    
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
