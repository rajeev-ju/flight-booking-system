package com.airline.booking.cache

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*

@Component
class SeatBlockingCache(
    @Qualifier("customReactiveRedisTemplate")
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    companion object {
        private const val SEAT_AVAILABILITY_KEY = "flight:schedule:%s:seats"
        private const val BOOKING_SEATS_KEY = "booking:%s:seats"
        private const val SEAT_BLOCK_TTL_MINUTES = 10L
        private const val SEAT_AVAILABILITY_TTL_HOURS = 24L
    }
    
    /**
     * Get available seats for a flight schedule
     */
    suspend fun getAvailableSeats(flightScheduleId: UUID): Int? {
        val key = SEAT_AVAILABILITY_KEY.format(flightScheduleId)
        return try {
            redisTemplate.opsForHash<String, String>()
                .get(key, "available")
                .awaitFirstOrNull()
                ?.toIntOrNull()
        } catch (e: Exception) {
            logger.error("Error getting available seats for $flightScheduleId", e)
            null
        }
    }
    
    /**
     * Set available seats for a flight schedule
     */
    suspend fun setAvailableSeats(flightScheduleId: UUID, availableSeats: Int) {
        val key = SEAT_AVAILABILITY_KEY.format(flightScheduleId)
        try {
            redisTemplate.opsForHash<String, String>()
                .put(key, "available", availableSeats.toString())
                .awaitSingle()
            
            // Set TTL
            redisTemplate.expire(key, Duration.ofHours(SEAT_AVAILABILITY_TTL_HOURS))
                .awaitSingle()
                
            logger.debug("Set available seats for $flightScheduleId: $availableSeats")
        } catch (e: Exception) {
            logger.error("Error setting available seats for $flightScheduleId", e)
        }
    }
    
    /**
     * Block seats temporarily for booking
     * Returns true if seats were successfully blocked
     */
    suspend fun blockSeats(
        flightScheduleId: UUID,
        bookingId: UUID,
        numberOfSeats: Int
    ): Boolean {
        val availableKey = SEAT_AVAILABILITY_KEY.format(flightScheduleId)
        val bookingKey = BOOKING_SEATS_KEY.format(bookingId)
        
        return try {
            // Get current available seats
            val currentAvailable = getAvailableSeats(flightScheduleId) ?: return false
            
            if (currentAvailable < numberOfSeats) {
                logger.warn("Not enough seats available. Required: $numberOfSeats, Available: $currentAvailable")
                return false
            }
            
            // Decrement available seats
            val newAvailable = currentAvailable - numberOfSeats
            setAvailableSeats(flightScheduleId, newAvailable)
            
            // Store blocked seats info with TTL
            redisTemplate.opsForValue()
                .set(bookingKey, "$flightScheduleId:$numberOfSeats", Duration.ofMinutes(SEAT_BLOCK_TTL_MINUTES))
                .awaitSingle()
            
            // Update blocked count
            redisTemplate.opsForHash<String, String>()
                .increment(availableKey, "blocked", numberOfSeats.toLong())
                .awaitSingle()
            
            logger.info("Blocked $numberOfSeats seats for booking $bookingId on flight $flightScheduleId")
            true
        } catch (e: Exception) {
            logger.error("Error blocking seats for booking $bookingId", e)
            false
        }
    }
    
    /**
     * Release blocked seats (in case of failure or timeout)
     */
    suspend fun releaseSeats(bookingId: UUID) {
        val bookingKey = BOOKING_SEATS_KEY.format(bookingId)
        
        try {
            // Get blocked seats info
            val blockedInfo = redisTemplate.opsForValue()
                .get(bookingKey)
                .awaitFirstOrNull()
            
            if (blockedInfo != null) {
                val parts = blockedInfo.split(":")
                if (parts.size == 2) {
                    val flightScheduleId = UUID.fromString(parts[0])
                    val numberOfSeats = parts[1].toInt()
                    
                    // Increment available seats
                    val currentAvailable = getAvailableSeats(flightScheduleId) ?: 0
                    setAvailableSeats(flightScheduleId, currentAvailable + numberOfSeats)
                    
                    // Decrement blocked count
                    val availableKey = SEAT_AVAILABILITY_KEY.format(flightScheduleId)
                    redisTemplate.opsForHash<String, String>()
                        .increment(availableKey, "blocked", -numberOfSeats.toLong())
                        .awaitSingle()
                    
                    // Delete booking key
                    redisTemplate.delete(bookingKey).awaitSingle()
                    
                    logger.info("Released $numberOfSeats seats for booking $bookingId")
                }
            }
        } catch (e: Exception) {
            logger.error("Error releasing seats for booking $bookingId", e)
        }
    }
    
    /**
     * Confirm blocked seats (convert from blocked to confirmed)
     */
    suspend fun confirmSeats(bookingId: UUID) {
        val bookingKey = BOOKING_SEATS_KEY.format(bookingId)
        
        try {
            // Get blocked seats info
            val blockedInfo = redisTemplate.opsForValue()
                .get(bookingKey)
                .awaitFirstOrNull()
            
            if (blockedInfo != null) {
                val parts = blockedInfo.split(":")
                if (parts.size == 2) {
                    val flightScheduleId = UUID.fromString(parts[0])
                    val numberOfSeats = parts[1].toInt()
                    
                    val availableKey = SEAT_AVAILABILITY_KEY.format(flightScheduleId)
                    
                    // Move from blocked to confirmed
                    redisTemplate.opsForHash<String, String>()
                        .increment(availableKey, "blocked", -numberOfSeats.toLong())
                        .awaitSingle()
                    
                    redisTemplate.opsForHash<String, String>()
                        .increment(availableKey, "confirmed", numberOfSeats.toLong())
                        .awaitSingle()
                    
                    // Delete booking key as it's now confirmed
                    redisTemplate.delete(bookingKey).awaitSingle()
                    
                    logger.info("Confirmed $numberOfSeats seats for booking $bookingId")
                }
            }
        } catch (e: Exception) {
            logger.error("Error confirming seats for booking $bookingId", e)
        }
    }
    
    /**
     * Initialize seat availability from flight management service
     */
    suspend fun initializeSeatAvailability(
        flightScheduleId: UUID,
        totalSeats: Int,
        bookedSeats: Int = 0
    ) {
        val key = SEAT_AVAILABILITY_KEY.format(flightScheduleId)
        val availableSeats = totalSeats - bookedSeats
        
        try {
            val updates = mapOf(
                "available" to availableSeats.toString(),
                "blocked" to "0",
                "confirmed" to bookedSeats.toString(),
                "total" to totalSeats.toString()
            )
            
            redisTemplate.opsForHash<String, String>()
                .putAll(key, updates)
                .awaitSingle()
            
            redisTemplate.expire(key, Duration.ofHours(SEAT_AVAILABILITY_TTL_HOURS))
                .awaitSingle()
                
            logger.info("Initialized seat availability for $flightScheduleId: $availableSeats/$totalSeats available")
        } catch (e: Exception) {
            logger.error("Error initializing seat availability for $flightScheduleId", e)
        }
    }
}
