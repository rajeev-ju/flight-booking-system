package com.airline.search.service

import com.airline.search.dtos.ConnectingFlightOption
import com.airline.search.dtos.FlightOption
import com.airline.search.model.FlightRouteDocument
import com.airline.search.repository.FlightRouteRepository
import com.airline.shared.dto.FlightSearchRequest
import com.airline.shared.dto.FlightType
import com.airline.shared.enums.SortOption
import com.airline.shared.exception.SearchServiceException
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FlightSearchServiceTest {

    private val flightRouteRepository = mockk<FlightRouteRepository>()
    private val flightSearchService = FlightSearchService(flightRouteRepository)

    @Test
    fun `searchFlights should return flights with summary when precomputed routes exist for given origin destination and date`() = runTest {
        // Given
        val request = createSearchRequest()
        val routeDocument = createRouteDocument()
        
        every { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        } returns Mono.just(routeDocument)

        // When
        val result = flightSearchService.searchFlights(request)

        // Then
        assertNotNull(result)
        assertEquals(2, result.totalResults) // 1 direct + 1 connecting
        assertEquals(2, result.results.size)
        assertTrue(result.fromCache)
        assertNotNull(result.searchId)
        assertTrue(result.searchTimeMs >= 0)
        
        // Verify summary
        assertNotNull(result.summary)
        assertEquals(1, result.summary.directFlightCount)
        assertEquals(1, result.summary.connectingFlightCount)
        assertNotNull(result.summary.cheapestFlight)
        assertNotNull(result.summary.fastestFlight)
        
        verify { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        }
    }

    @Test
    fun `searchFlights should return empty results when no precomputed routes exist for given route`() = runTest {
        // Given
        val request = createSearchRequest()
        
        every { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        } returns Mono.empty()

        // When
        val result = flightSearchService.searchFlights(request)

        // Then
        assertNotNull(result)
        assertEquals(0, result.totalResults)
        assertTrue(result.results.isEmpty())
        assertTrue(!result.fromCache)
        assertNotNull(result.searchId)
        
        // Verify empty summary
        assertEquals(0, result.summary.directFlightCount)
        assertEquals(0, result.summary.connectingFlightCount)
        assertEquals(null, result.summary.cheapestFlight)
        assertEquals(null, result.summary.fastestFlight)
        
        verify { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        }
    }

    @Test
    fun `searchFlights should filter out flights with insufficient available seats`() = runTest {
        // Given
        val request = createSearchRequest(passengers = 5)
        val routeDocument = createRouteDocumentWithLimitedSeats()
        
        every { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        } returns Mono.just(routeDocument)

        // When
        val result = flightSearchService.searchFlights(request)

        // Then
        assertEquals(1, result.totalResults) // Only connecting flight has enough seats
        assertEquals(FlightType.CONNECTING, result.results[0].type)
        assertTrue(result.results[0].availableSeats >= request.passengers)
    }

    @Test
    fun `searchFlights should exclude connecting flights when includeConnecting is false`() = runTest {
        // Given
        val request = createSearchRequest(includeConnecting = false)
        val routeDocument = createRouteDocument()
        
        every { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        } returns Mono.just(routeDocument)

        // When
        val result = flightSearchService.searchFlights(request)

        // Then
        assertEquals(1, result.totalResults)
        assertEquals(FlightType.DIRECT, result.results[0].type)
        assertEquals(1, result.summary.directFlightCount)
        assertEquals(0, result.summary.connectingFlightCount)
    }

    @Test
    fun `searchFlights should sort results by price when sortBy is PRICE`() = runTest {
        // Given
        val request = createSearchRequest(sortBy = SortOption.PRICE)
        val routeDocument = createRouteDocumentWithMultipleFlights()
        
        every { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        } returns Mono.just(routeDocument)

        // When
        val result = flightSearchService.searchFlights(request)

        // Then
        assertTrue(result.results.size > 1)
        for (i in 0 until result.results.size - 1) {
            assertTrue(result.results[i].totalPrice <= result.results[i + 1].totalPrice)
        }
    }

    @Test
    fun `searchFlights should sort results by duration when sortBy is DURATION`() = runTest {
        // Given
        val request = createSearchRequest(sortBy = SortOption.DURATION)
        val routeDocument = createRouteDocumentWithMultipleFlights()
        
        every { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        } returns Mono.just(routeDocument)

        // When
        val result = flightSearchService.searchFlights(request)

        // Then
        assertTrue(result.results.size > 1)
        for (i in 0 until result.results.size - 1) {
            assertTrue(result.results[i].totalDuration <= result.results[i + 1].totalDuration)
        }
    }

    @Test
    fun `searchFlights should limit results to maxResults when more flights are available`() = runTest {
        // Given
        val maxResults = 2
        val request = createSearchRequest(maxResults = maxResults)
        val routeDocument = createRouteDocumentWithManyFlights()
        
        every { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        } returns Mono.just(routeDocument)

        // When
        val result = flightSearchService.searchFlights(request)

        // Then
        assertEquals(maxResults, result.results.size)
        assertEquals(maxResults, result.totalResults)
    }

    @Test
    fun `searchFlights should calculate correct total price based on number of passengers`() = runTest {
        // Given
        val passengers = 3
        val request = createSearchRequest(passengers = passengers)
        val routeDocument = createRouteDocument()
        
        every { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        } returns Mono.just(routeDocument)

        // When
        val result = flightSearchService.searchFlights(request)

        // Then
        val directFlight = result.results.find { it.type == FlightType.DIRECT }
        assertNotNull(directFlight)
        assertEquals(5000.0 * passengers, directFlight.totalPrice)
    }

    @Test
    fun `searchFlights should throw SearchServiceException when repository throws exception`() = runTest {
        // Given
        val request = createSearchRequest()
        val errorMessage = "Database connection failed"
        
        every { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        } returns Mono.error(RuntimeException(errorMessage))

        // When & Then
        val exception = assertThrows<SearchServiceException> {
            flightSearchService.searchFlights(request)
        }
        
        assertEquals(errorMessage, exception.message)
    }

    @Test
    fun `searchFlights should correctly identify cheapest and fastest flights in summary`() = runTest {
        // Given
        val request = createSearchRequest()
        val routeDocument = createRouteDocumentWithVariedPricesAndDurations()
        
        every { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        } returns Mono.just(routeDocument)

        // When
        val result = flightSearchService.searchFlights(request)

        // Then
        assertNotNull(result.summary.cheapestFlight)
        assertNotNull(result.summary.fastestFlight)
        assertEquals(4000.0, result.summary.cheapestFlight?.totalPrice)
        assertEquals(90, result.summary.fastestFlight?.totalDuration)
    }

    @Test
    fun `searchFlights should calculate correct price and duration ranges in summary`() = runTest {
        // Given
        val request = createSearchRequest()
        val routeDocument = createRouteDocumentWithVariedPricesAndDurations()
        
        every { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        } returns Mono.just(routeDocument)

        // When
        val result = flightSearchService.searchFlights(request)

        // Then
        assertEquals(4000.0, result.summary.priceRange.min)
        assertEquals(8000.0, result.summary.priceRange.max)
        assertEquals(6000.0, result.summary.priceRange.average)
        
        assertEquals(90, result.summary.durationRange.min)
        assertEquals(210, result.summary.durationRange.max)
        assertEquals(150, result.summary.durationRange.average)
    }

    @Test
    fun `searchFlights should extract distinct airlines from all flight segments`() = runTest {
        // Given
        val request = createSearchRequest()
        val routeDocument = createRouteDocumentWithMultipleAirlines()
        
        every { 
            flightRouteRepository.findByOriginAndDestinationAndDate(
                request.origin, 
                request.destination, 
                request.departureDate
            ) 
        } returns Mono.just(routeDocument)

        // When
        val result = flightSearchService.searchFlights(request)

        // Then
        assertEquals(3, result.summary.availableAirlines.size)
        assertTrue(result.summary.availableAirlines.contains("AI"))
        assertTrue(result.summary.availableAirlines.contains("6E"))
        assertTrue(result.summary.availableAirlines.contains("SG"))
    }

    // Helper methods to create test data
    private fun createSearchRequest(
        origin: String = "DEL",
        destination: String = "BOM",
        departureDate: LocalDate = LocalDate.now().plusDays(7),
        passengers: Int = 1,
        includeConnecting: Boolean = true,
        sortBy: SortOption = SortOption.PRICE,
        maxResults: Int = 20
    ) = FlightSearchRequest(
        origin = origin,
        destination = destination,
        departureDate = departureDate,
        passengers = passengers,
        includeConnecting = includeConnecting,
        sortBy = sortBy,
        maxResults = maxResults
    )

    private fun createRouteDocument() = FlightRouteDocument(
        id = "DEL_BOM_2025-10-05",
        origin = "DEL",
        destination = "BOM",
        date = LocalDate.now().plusDays(7),
        directFlights = listOf(
            FlightOption(
                scheduleId = UUID.randomUUID(),
                flightNumber = "AI101",
                airline = "AI",
                departureTime = "2025-10-05T06:00:00",
                arrivalTime = "2025-10-05T08:30:00",
                duration = 150,
                price = 5000.0,
                availableSeats = 50,
                aircraft = "Boeing 737"
            )
        ),
        connectingFlights = listOf(
            ConnectingFlightOption(
                id = "CON_001",
                segments = listOf(
                    FlightOption(
                        scheduleId = UUID.randomUUID(),
                        flightNumber = "6E201",
                        airline = "6E",
                        departureTime = "2025-10-05T07:00:00",
                        arrivalTime = "2025-10-05T09:00:00",
                        duration = 120,
                        price = 3000.0,
                        availableSeats = 30,
                        aircraft = "Airbus A320"
                    ),
                    FlightOption(
                        scheduleId = UUID.randomUUID(),
                        flightNumber = "6E202",
                        airline = "6E",
                        departureTime = "2025-10-05T10:00:00",
                        arrivalTime = "2025-10-05T11:30:00",
                        duration = 90,
                        price = 2500.0,
                        availableSeats = 25,
                        aircraft = "Airbus A320"
                    )
                ),
                totalPrice = 5500.0,
                totalDuration = 270,
                minAvailableSeats = 25,
                layoverAirport = "BLR",
                layoverDuration = 60
            )
        ),
        minPrice = 5000.0,
        minDuration = 150
    )

    private fun createRouteDocumentWithLimitedSeats() = FlightRouteDocument(
        id = "DEL_BOM_2025-10-05",
        origin = "DEL",
        destination = "BOM",
        date = LocalDate.now().plusDays(7),
        directFlights = listOf(
            FlightOption(
                scheduleId = UUID.randomUUID(),
                flightNumber = "AI101",
                airline = "AI",
                departureTime = "2025-10-05T06:00:00",
                arrivalTime = "2025-10-05T08:30:00",
                duration = 150,
                price = 5000.0,
                availableSeats = 3, // Less than 5 passengers
                aircraft = "Boeing 737"
            )
        ),
        connectingFlights = listOf(
            ConnectingFlightOption(
                id = "CON_001",
                segments = listOf(
                    FlightOption(
                        scheduleId = UUID.randomUUID(),
                        flightNumber = "6E201",
                        airline = "6E",
                        departureTime = "2025-10-05T07:00:00",
                        arrivalTime = "2025-10-05T09:00:00",
                        duration = 120,
                        price = 3000.0,
                        availableSeats = 10,
                        aircraft = "Airbus A320"
                    )
                ),
                totalPrice = 5500.0,
                totalDuration = 270,
                minAvailableSeats = 10, // Enough for 5 passengers
                layoverAirport = "BLR",
                layoverDuration = 60
            )
        ),
        minPrice = 5000.0,
        minDuration = 150
    )

    private fun createRouteDocumentWithMultipleFlights() = FlightRouteDocument(
        id = "DEL_BOM_2025-10-05",
        origin = "DEL",
        destination = "BOM",
        date = LocalDate.now().plusDays(7),
        directFlights = listOf(
            FlightOption(
                scheduleId = UUID.randomUUID(),
                flightNumber = "AI101",
                airline = "AI",
                departureTime = "2025-10-05T06:00:00",
                arrivalTime = "2025-10-05T08:30:00",
                duration = 150,
                price = 6000.0,
                availableSeats = 50,
                aircraft = "Boeing 737"
            ),
            FlightOption(
                scheduleId = UUID.randomUUID(),
                flightNumber = "6E301",
                airline = "6E",
                departureTime = "2025-10-05T08:00:00",
                arrivalTime = "2025-10-05T10:00:00",
                duration = 120,
                price = 4500.0,
                availableSeats = 60,
                aircraft = "Airbus A320"
            )
        ),
        connectingFlights = listOf(
            ConnectingFlightOption(
                id = "CON_001",
                segments = emptyList(),
                totalPrice = 5500.0,
                totalDuration = 180,
                minAvailableSeats = 25,
                layoverAirport = "BLR",
                layoverDuration = 60
            )
        ),
        minPrice = 4500.0,
        minDuration = 120
    )

    private fun createRouteDocumentWithManyFlights() = FlightRouteDocument(
        id = "DEL_BOM_2025-10-05",
        origin = "DEL",
        destination = "BOM",
        date = LocalDate.now().plusDays(7),
        directFlights = (1..5).map { i ->
            FlightOption(
                scheduleId = UUID.randomUUID(),
                flightNumber = "AI10$i",
                airline = "AI",
                departureTime = "2025-10-05T${String.format("%02d", 5+i)}:00:00",
                arrivalTime = "2025-10-05T${String.format("%02d", 7+i)}:30:00",
                duration = 150,
                price = 5000.0 + (i * 100),
                availableSeats = 50,
                aircraft = "Boeing 737"
            )
        },
        connectingFlights = emptyList(),
        minPrice = 5000.0,
        minDuration = 150
    )

    private fun createRouteDocumentWithVariedPricesAndDurations() = FlightRouteDocument(
        id = "DEL_BOM_2025-10-05",
        origin = "DEL",
        destination = "BOM",
        date = LocalDate.now().plusDays(7),
        directFlights = listOf(
            FlightOption(
                scheduleId = UUID.randomUUID(),
                flightNumber = "AI101",
                airline = "AI",
                departureTime = "2025-10-05T06:00:00",
                arrivalTime = "2025-10-05T08:30:00",
                duration = 150,
                price = 6000.0,
                availableSeats = 50,
                aircraft = "Boeing 737"
            ),
            FlightOption(
                scheduleId = UUID.randomUUID(),
                flightNumber = "6E301",
                airline = "6E",
                departureTime = "2025-10-05T08:00:00",
                arrivalTime = "2025-10-05T09:30:00",
                duration = 90,
                price = 8000.0,
                availableSeats = 60,
                aircraft = "Airbus A320"
            ),
            FlightOption(
                scheduleId = UUID.randomUUID(),
                flightNumber = "SG401",
                airline = "SG",
                departureTime = "2025-10-05T10:00:00",
                arrivalTime = "2025-10-05T13:30:00",
                duration = 210,
                price = 4000.0,
                availableSeats = 40,
                aircraft = "Boeing 737"
            )
        ),
        connectingFlights = emptyList(),
        minPrice = 4000.0,
        minDuration = 90
    )

    private fun createRouteDocumentWithMultipleAirlines() = FlightRouteDocument(
        id = "DEL_BOM_2025-10-05",
        origin = "DEL",
        destination = "BOM",
        date = LocalDate.now().plusDays(7),
        directFlights = listOf(
            FlightOption(
                scheduleId = UUID.randomUUID(),
                flightNumber = "AI101",
                airline = "AI",
                departureTime = "2025-10-05T06:00:00",
                arrivalTime = "2025-10-05T08:30:00",
                duration = 150,
                price = 5000.0,
                availableSeats = 50,
                aircraft = "Boeing 737"
            ),
            FlightOption(
                scheduleId = UUID.randomUUID(),
                flightNumber = "6E301",
                airline = "6E",
                departureTime = "2025-10-05T08:00:00",
                arrivalTime = "2025-10-05T10:00:00",
                duration = 120,
                price = 4500.0,
                availableSeats = 60,
                aircraft = "Airbus A320"
            )
        ),
        connectingFlights = listOf(
            ConnectingFlightOption(
                id = "CON_001",
                segments = listOf(
                    FlightOption(
                        scheduleId = UUID.randomUUID(),
                        flightNumber = "SG501",
                        airline = "SG",
                        departureTime = "2025-10-05T07:00:00",
                        arrivalTime = "2025-10-05T09:00:00",
                        duration = 120,
                        price = 3000.0,
                        availableSeats = 30,
                        aircraft = "Boeing 737"
                    )
                ),
                totalPrice = 5500.0,
                totalDuration = 270,
                minAvailableSeats = 25,
                layoverAirport = "BLR",
                layoverDuration = 60
            )
        ),
        minPrice = 4500.0,
        minDuration = 120
    )
}
