package com.airline.booking.cache

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.ReactiveValueOperations
import org.springframework.data.redis.core.ReactiveHashOperations
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SeatBlockingCacheTest {

    private lateinit var redisTemplate: ReactiveRedisTemplate<String, String>
    private lateinit var valueOps: ReactiveValueOperations<String, String>
    private lateinit var hashOps: ReactiveHashOperations<String, String, String>
    private lateinit var seatBlockingCache: SeatBlockingCache

    @BeforeEach
    fun setUp() {
        redisTemplate = mockk()
        valueOps = mockk()
        hashOps = mockk()
        seatBlockingCache = SeatBlockingCache(redisTemplate)
        
        every { redisTemplate.opsForValue() } returns valueOps
        every { redisTemplate.opsForHash<String, String>() } returns hashOps
    }

    @Test
    fun `should successfully block seats when seats are available`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val bookingId = UUID.randomUUID()
        val numberOfSeats = 2
        val availableSeatsKey = "flight:schedule:$flightScheduleId:seats"
        val bookingKey = "booking:$bookingId:seats"
        
        // Mock getAvailableSeats
        every { 
            hashOps.get(availableSeatsKey, "available") 
        } returns Mono.just("50")
        
        // Mock setAvailableSeats
        every { 
            hashOps.put(availableSeatsKey, "available", "48") 
        } returns Mono.just(true)
        
        // Mock storing blocked seats info
        every { 
            valueOps.set(bookingKey, "$flightScheduleId:$numberOfSeats", Duration.ofMinutes(10)) 
        } returns Mono.just(true)
        
        // Mock increment blocked count
        every { 
            hashOps.increment(availableSeatsKey, "blocked", numberOfSeats.toLong()) 
        } returns Mono.just(2L)

        // When
        val result = seatBlockingCache.blockSeats(flightScheduleId, bookingId, numberOfSeats)

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { hashOps.get(availableSeatsKey, "available") }
        coVerify(exactly = 1) { hashOps.put(availableSeatsKey, "available", "48") }
        coVerify(exactly = 1) { valueOps.set(bookingKey, "$flightScheduleId:$numberOfSeats", Duration.ofMinutes(10)) }
        coVerify(exactly = 1) { hashOps.increment(availableSeatsKey, "blocked", numberOfSeats.toLong()) }
    }

    @Test
    fun `should return false when insufficient seats are available`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val bookingId = UUID.randomUUID()
        val numberOfSeats = 5
        val availableSeatsKey = "flight:schedule:$flightScheduleId:seats"
        
        // Mock getAvailableSeats with insufficient seats
        every { 
            hashOps.get(availableSeatsKey, "available") 
        } returns Mono.just("2")

        // When
        val result = seatBlockingCache.blockSeats(flightScheduleId, bookingId, numberOfSeats)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { hashOps.get(availableSeatsKey, "available") }
        coVerify(exactly = 0) { hashOps.put(any(), any(), any()) }
    }

    @Test
    fun `should return false when available seats key not found in cache`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val bookingId = UUID.randomUUID()
        val numberOfSeats = 2
        val availableSeatsKey = "flight:schedule:$flightScheduleId:seats"
        
        // Mock getAvailableSeats returns null
        every { 
            hashOps.get(availableSeatsKey, "available") 
        } returns Mono.empty()

        // When
        val result = seatBlockingCache.blockSeats(flightScheduleId, bookingId, numberOfSeats)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { hashOps.get(availableSeatsKey, "available") }
        coVerify(exactly = 0) { hashOps.put(any(), any(), any()) }
    }

    @Test
    fun `should successfully release blocked seats`() = runBlocking {
        // Given
        val bookingId = UUID.randomUUID()
        val flightScheduleId = UUID.randomUUID()
        val numberOfSeats = 2
        val bookingKey = "booking:$bookingId:seats"
        val availableSeatsKey = "flight:schedule:$flightScheduleId:seats"
        
        // Mock getting blocked seats info
        every { 
            valueOps.get(bookingKey) 
        } returns Mono.just("$flightScheduleId:$numberOfSeats")
        
        // Mock getAvailableSeats
        every { 
            hashOps.get(availableSeatsKey, "available") 
        } returns Mono.just("48")
        
        // Mock setAvailableSeats
        every { 
            hashOps.put(availableSeatsKey, "available", "50") 
        } returns Mono.just(true)
        
        // Mock decrement blocked count
        every { 
            hashOps.increment(availableSeatsKey, "blocked", -numberOfSeats.toLong()) 
        } returns Mono.just(0L)
        
        // Mock delete booking key
        every { 
            redisTemplate.delete(bookingKey) 
        } returns Mono.just(1L)

        // When
        seatBlockingCache.releaseSeats(bookingId)

        // Then
        coVerify(exactly = 1) { valueOps.get(bookingKey) }
        coVerify(exactly = 1) { hashOps.get(availableSeatsKey, "available") }
        coVerify(exactly = 1) { hashOps.put(availableSeatsKey, "available", "50") }
        coVerify(exactly = 1) { hashOps.increment(availableSeatsKey, "blocked", -numberOfSeats.toLong()) }
        coVerify(exactly = 1) { redisTemplate.delete(bookingKey) }
    }

    @Test
    fun `should handle release when no blocked seats found`() = runBlocking {
        // Given
        val bookingId = UUID.randomUUID()
        val bookingKey = "booking:$bookingId:seats"
        
        // Mock no blocked seats info
        every { 
            valueOps.get(bookingKey) 
        } returns Mono.empty()

        // When
        seatBlockingCache.releaseSeats(bookingId)

        // Then
        coVerify(exactly = 1) { valueOps.get(bookingKey) }
        coVerify(exactly = 0) { hashOps.get(any(), any()) }
        coVerify(exactly = 0) { hashOps.put(any(), any(), any()) }
        coVerify(exactly = 0) { redisTemplate.delete(any<String>()) }
    }

    @Test
    fun `should handle exception when blocking seats fails`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val bookingId = UUID.randomUUID()
        val numberOfSeats = 2
        val availableSeatsKey = "flight:schedule:$flightScheduleId:seats"
        
        // Mock exception when getting available seats
        every { 
            hashOps.get(availableSeatsKey, "available") 
        } returns Mono.error(RuntimeException("Redis connection error"))

        // When
        val result = seatBlockingCache.blockSeats(flightScheduleId, bookingId, numberOfSeats)

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { hashOps.get(availableSeatsKey, "available") }
    }

    @Test
    fun `should handle exception when confirming seats fails`() = runBlocking {
        // Given
        val bookingId = UUID.randomUUID()
        val bookingKey = "booking:$bookingId:seats"
        
        // Mock exception when getting booking info
        every { 
            valueOps.get(bookingKey) 
        } returns Mono.error(RuntimeException("Redis connection error"))

        // When
        seatBlockingCache.confirmSeats(bookingId)

        // Then
        coVerify(exactly = 1) { valueOps.get(bookingKey) }
        coVerify(exactly = 0) { hashOps.increment(any(), any(), any<Long>()) }
        coVerify(exactly = 0) { redisTemplate.delete(any<String>()) }
    }
}
