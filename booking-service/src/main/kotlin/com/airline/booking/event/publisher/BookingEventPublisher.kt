package com.airline.booking.event.publisher

import com.airline.booking.event.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class BookingEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    companion object {
        private const val BOOKING_EVENTS_TOPIC = "booking-events"
        private const val SEAT_UPDATE_TOPIC = "seat-updates"
    }
    
    suspend fun publishBookingCreated(event: BookingCreatedEvent) {
        try {
            val eventJson = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.bookingId.toString(), eventJson)
            logger.info("Published BookingCreatedEvent for booking ${event.bookingId}")
        } catch (e: Exception) {
            logger.error("Failed to publish BookingCreatedEvent for booking ${event.bookingId}", e)
        }
    }
    
    suspend fun publishBookingCancelled(event: BookingCancelledEvent) {
        try {
            val eventJson = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.bookingId.toString(), eventJson)
            logger.info("Published BookingCancelledEvent for booking ${event.bookingId}")
        } catch (e: Exception) {
            logger.error("Failed to publish BookingCancelledEvent for booking ${event.bookingId}", e)
        }
    }
    
    suspend fun publishSeatUpdate(event: SeatUpdateEvent) {
        try {
            val eventJson = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(SEAT_UPDATE_TOPIC, event.flightScheduleId.toString(), eventJson)
            logger.info("Published SeatUpdateEvent for flight ${event.flightScheduleId}, operation: ${event.operation}")
        } catch (e: Exception) {
            logger.error("Failed to publish SeatUpdateEvent for flight ${event.flightScheduleId}", e)
        }
    }
    
    suspend fun publishBookingStatusChanged(event: BookingStatusChangedEvent) {
        try {
            val eventJson = objectMapper.writeValueAsString(event)
            kafkaTemplate.send(BOOKING_EVENTS_TOPIC, event.bookingId.toString(), eventJson)
            logger.info("Published BookingStatusChangedEvent for booking ${event.bookingId}: ${event.oldStatus} -> ${event.newStatus}")
        } catch (e: Exception) {
            logger.error("Failed to publish BookingStatusChangedEvent for booking ${event.bookingId}", e)
        }
    }
}
