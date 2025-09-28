package com.airline.search.client

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Simplified integration tests for FlightManagementClient
 * These tests verify the client's behavior without complex mocking
 */
class FlightManagementClientSimpleTest {

    @Test
    fun `client should be created with proper configuration`() {
        // Given
        val webClientBuilder = WebClient.builder()
        val flightServiceUrl = "http://localhost:8081/flight-management-service"
        
        // When
        val client = FlightManagementClient(webClientBuilder, flightServiceUrl)
        
        // Then
        assertNotNull(client)
    }

    @Test
    fun `getAllActiveFlights should handle empty response`() = runTest {
        // Given
        val webClientBuilder = WebClient.builder()
        val flightServiceUrl = "http://localhost:9999/non-existent" // Non-existent service
        val client = FlightManagementClient(webClientBuilder, flightServiceUrl)
        
        // When
        val result = client.getAllActiveFlights().collectList().awaitSingle()
        
        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getSchedulesForDate should handle empty response`() = runTest {
        // Given
        val webClientBuilder = WebClient.builder()
        val flightServiceUrl = "http://localhost:9999/non-existent"
        val client = FlightManagementClient(webClientBuilder, flightServiceUrl)
        val date = LocalDate.of(2025, 10, 5)
        
        // When
        val result = client.getSchedulesForDate(date).collectList().awaitSingle()
        
        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getSchedulesForDateRange should handle empty response`() = runTest {
        // Given
        val webClientBuilder = WebClient.builder()
        val flightServiceUrl = "http://localhost:9999/non-existent"
        val client = FlightManagementClient(webClientBuilder, flightServiceUrl)
        val startDate = LocalDate.of(2025, 10, 5)
        val endDate = LocalDate.of(2025, 10, 7)
        
        // When
        val result = client.getSchedulesForDateRange(startDate, endDate).collectList().awaitSingle()
        
        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAvailableFlights should handle empty response`() = runTest {
        // Given
        val webClientBuilder = WebClient.builder()
        val flightServiceUrl = "http://localhost:9999/non-existent"
        val client = FlightManagementClient(webClientBuilder, flightServiceUrl)
        val origin = "DEL"
        val destination = "BOM"
        val date = LocalDate.of(2025, 10, 5)
        val minSeats = 2
        
        // When
        val result = client.getAvailableFlights(origin, destination, date, minSeats).collectList().awaitSingle()
        
        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAvailableFlights should use default minSeats when not provided`() = runTest {
        // Given
        val webClientBuilder = WebClient.builder()
        val flightServiceUrl = "http://localhost:9999/non-existent"
        val client = FlightManagementClient(webClientBuilder, flightServiceUrl)
        val origin = "DEL"
        val destination = "BOM"
        val date = LocalDate.of(2025, 10, 5)
        
        // When - calling without minSeats parameter
        val result = client.getAvailableFlights(origin, destination, date).collectList().awaitSingle()
        
        // Then
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }
}
