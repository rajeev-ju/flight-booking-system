package com.airline.flight.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * Airline Entity - Represents an airline company
 */
@Table("airlines")
data class AirlineEntity(
    @Id
    val id: UUID? = null,
    
    val code: String, // 2-letter IATA code like "AI"
    
    val name: String,
    
    val country: String = "India",
    
    @Column("is_active")
    val active: Boolean = true,
    
    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
