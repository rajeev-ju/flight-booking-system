package com.airline.booking.client

import com.airline.booking.exception.FlightServiceException
import com.airline.shared.dto.ApiResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FlightManagementClientTest {

    private lateinit var webClient: WebClient
    private lateinit var requestHeadersUriSpec: RequestHeadersUriSpec<*>
    private lateinit var requestHeadersSpec: RequestHeadersSpec<*>
    private lateinit var requestBodyUriSpec: RequestBodyUriSpec
    private lateinit var requestBodySpec: RequestBodySpec
    private lateinit var responseSpec: ResponseSpec
    private lateinit var flightManagementClient: FlightManagementClient

    @BeforeEach
    fun setUp() {
        webClient = mockk()
        requestHeadersUriSpec = mockk()
        requestHeadersSpec = mockk()
        requestBodyUriSpec = mockk()
        requestBodySpec = mockk()
        responseSpec = mockk()
        
        flightManagementClient = FlightManagementClient(webClient)
        
        // Set the flight management URL using reflection
        val urlField = FlightManagementClient::class.java.getDeclaredField("flightManagementUrl")
        urlField.isAccessible = true
        urlField.set(flightManagementClient, "http://localhost:8081")
    }

    @Test
    fun `should successfully check seat availability when seats are available`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val numberOfSeats = 2
        val availableSeats = 50
        
        val apiResponse = ApiResponse.success(
            mapOf(
                "scheduleId" to flightScheduleId.toString(),
                "availableSeats" to availableSeats
            )
        )
        
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(any<ParameterizedTypeReference<*>>()) } answers {
            Mono.just(apiResponse)
        }

        // When
        val result = flightManagementClient.checkSeatAvailability(flightScheduleId, numberOfSeats)

        // Then
        assertTrue(result.available)
        assertEquals(availableSeats, result.availableSeats)
        assertEquals(numberOfSeats, result.requestedSeats)
        
        verify(exactly = 1) { webClient.get() }
        verify(exactly = 1) { 
            requestHeadersUriSpec.uri("http://localhost:8081/api/schedules/$flightScheduleId/seats") 
        }
    }

    @Test
    fun `should return unavailable when requested seats exceed available seats`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val numberOfSeats = 10
        val availableSeats = 5
        
        val apiResponse = ApiResponse.success(
            mapOf(
                "scheduleId" to flightScheduleId.toString(),
                "availableSeats" to availableSeats
            )
        )
        
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(any<ParameterizedTypeReference<*>>()) } answers {
            Mono.just(apiResponse)
        }

        // When
        val result = flightManagementClient.checkSeatAvailability(flightScheduleId, numberOfSeats)

        // Then
        assertFalse(result.available)
        assertEquals(availableSeats, result.availableSeats)
        assertEquals(numberOfSeats, result.requestedSeats)
    }

    @Test
    fun `should throw FlightServiceException when checking seat availability fails`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val numberOfSeats = 2
        
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(any<ParameterizedTypeReference<*>>()) } returns Mono.error(RuntimeException("Network error"))

        // When & Then
        assertThrows<FlightServiceException> {
            flightManagementClient.checkSeatAvailability(flightScheduleId, numberOfSeats)
        }
    }

    @Test
    fun `should successfully reserve seats and return true when successful`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val numberOfSeats = 2
        val apiResponse = ApiResponse.success("OK")
        
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(any<ParameterizedTypeReference<*>>()) } answers {
            Mono.just(apiResponse)
        }

        // When
        val result = flightManagementClient.reserveSeats(flightScheduleId, numberOfSeats)

        // Then
        assertTrue(result)
        
        verify(exactly = 1) { webClient.post() }
        verify(exactly = 1) { 
            requestBodyUriSpec.uri("http://localhost:8081/api/schedules/$flightScheduleId/reserve") 
        }
        verify(exactly = 1) { requestBodySpec.bodyValue(SeatOperationRequest(seats = numberOfSeats)) }
    }

    @Test
    fun `should return false when reserve seats operation fails`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val numberOfSeats = 2
        
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(any<ParameterizedTypeReference<*>>()) } returns Mono.error(RuntimeException("Service error"))

        // When
        val result = flightManagementClient.reserveSeats(flightScheduleId, numberOfSeats)

        // Then
        assertFalse(result)
    }

    @Test
    fun `should return false when release seats operation fails`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val numberOfSeats = 2
        
        every { webClient.post() } returns requestBodyUriSpec
        every { requestBodyUriSpec.uri(any<String>()) } returns requestBodySpec
        every { requestBodySpec.bodyValue(any()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(any<ParameterizedTypeReference<*>>()) } returns Mono.error(RuntimeException("Service error"))

        // When
        val result = flightManagementClient.releaseSeats(flightScheduleId, numberOfSeats)

        // Then
        assertFalse(result)
    }


    @Test
    fun `should return null when flight schedule details retrieval fails`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(any<Class<*>>()) } returns Mono.error(RuntimeException("Not found"))

        // When
        val result = flightManagementClient.getFlightScheduleDetails(flightScheduleId)

        // Then
        assertNull(result)
    }

    @Test
    fun `should handle null data in API response when checking seat availability`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val numberOfSeats = 2
        
        val apiResponse = ApiResponse.success(null)
        
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(any<ParameterizedTypeReference<*>>()) } answers {
            Mono.just(apiResponse)
        }

        // When & Then
        assertThrows<FlightServiceException> {
            flightManagementClient.checkSeatAvailability(flightScheduleId, numberOfSeats)
        }
    }

    @Test
    fun `should handle invalid availableSeats value in API response`() = runBlocking {
        // Given
        val flightScheduleId = UUID.randomUUID()
        val numberOfSeats = 2
        
        val apiResponse = ApiResponse.success(
            mapOf(
                "scheduleId" to flightScheduleId.toString(),
                "availableSeats" to "invalid" // Not a number
            )
        )
        
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.onStatus(any(), any()) } returns responseSpec
        every { responseSpec.bodyToMono(any<ParameterizedTypeReference<*>>()) } answers {
            Mono.just(apiResponse)
        }

        // When & Then
        assertThrows<FlightServiceException> {
            flightManagementClient.checkSeatAvailability(flightScheduleId, numberOfSeats)
        }
    }
}
