package com.airline.flight.listener

import com.airline.flight.event.model.SeatOperation
import com.airline.flight.event.model.SeatUpdateEvent
import com.airline.flight.service.FlightManagementService
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

/**
 * Listens to booking events and updates seat availability
 */
@Component
class BookingEventListener(
    private val flightManagementService: FlightManagementService,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Handle seat update events from booking service
     * Deserializes JSON message to SeatUpdateEvent
     */
    @KafkaListener(
        topics = ["seat-updates"],
        groupId = "flight-management-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleSeatUpdate(message: String, acknowledgment: Acknowledgment) {
        val event = try {
            objectMapper.readValue(message, SeatUpdateEvent::class.java)
        } catch (e: Exception) {
            logger.error("Failed to deserialize seat update event: $message", e)
            acknowledgment.acknowledge() // Acknowledge to avoid reprocessing
            return
        }
        logger.info("Received seat update event: $event")
        
        runBlocking {
            try {
                when (event.operation) {
                    SeatOperation.CONFIRM -> {
                        // Reduce available seats when booking is confirmed
                        logger.info("Reducing ${event.numberOfSeats} seats for schedule ${event.flightScheduleId}")
                        flightManagementService.reserveSeats(
                            event.flightScheduleId,
                            event.numberOfSeats
                        )
                    }
                    
                    SeatOperation.RELEASE -> {
                        // Increase available seats when booking is cancelled/failed
                        logger.info("Releasing ${event.numberOfSeats} seats for schedule ${event.flightScheduleId}")
                        flightManagementService.releaseSeats(
                            event.flightScheduleId,
                            event.numberOfSeats
                        )
                    }
                    
                    else -> {
                        logger.warn("Unknown seat operation: ${event.operation}")
                    }
                }
                
                logger.info("Successfully processed seat update for schedule ${event.flightScheduleId}")
                acknowledgment.acknowledge() // Acknowledge successful processing
                
            } catch (e: Exception) {
                logger.error("Error processing seat update event: $event", e)
                // Could implement retry logic or dead letter queue here
                // For now, acknowledge to avoid infinite retry
                acknowledgment.acknowledge()
            }
        }
    }
}
