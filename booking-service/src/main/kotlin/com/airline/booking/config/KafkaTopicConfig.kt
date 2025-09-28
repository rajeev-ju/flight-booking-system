package com.airline.booking.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

/**
 * Kafka topic configuration for booking service
 * Auto-creates topics if they don't exist
 */
@Configuration
class KafkaTopicConfig {
    
    companion object {
        const val SEAT_UPDATES_TOPIC = "seat-updates"
        const val BOOKING_EVENTS_TOPIC = "booking-events"
    }
    
    /**
     * Topic for seat inventory updates
     * Partitions: 3 for parallel processing
     * Replicas: 1 for development (increase for production)
     */
    @Bean
    fun seatUpdatesTopic(): NewTopic {
        return TopicBuilder
            .name(SEAT_UPDATES_TOPIC)
            .partitions(3)
            .replicas(1)
            .build()
    }
    
    /**
     * Topic for general booking events
     */
    @Bean
    fun bookingEventsTopic(): NewTopic {
        return TopicBuilder
            .name(BOOKING_EVENTS_TOPIC)
            .partitions(3)
            .replicas(1)
            .build()
    }
}
