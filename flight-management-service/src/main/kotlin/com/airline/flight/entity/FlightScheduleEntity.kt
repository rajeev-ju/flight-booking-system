package com.airline.flight.entity

import com.airline.shared.enums.FlightStatus
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.UUID

/**
 * Flight Schedule Entity - Represents a specific flight instance on a particular date
 */
@Table("flight_schedules")
data class FlightScheduleEntity(
    @Id
    val id: UUID,

    @Column("flight_number")
    val flightNumber: String,

    @Column("schedule_date")
    val scheduleDate: LocalDateTime,

    @Column("departure_date_time")
    val departureDateTime: LocalDateTime,

    @Column("arrival_date_time")
    val arrivalDateTime: LocalDateTime,

    @Column("available_seats")
    val availableSeats: Int,

    val price: Double,

    val status: FlightStatus = FlightStatus.SCHEDULED,

    @Column("created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column("updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
