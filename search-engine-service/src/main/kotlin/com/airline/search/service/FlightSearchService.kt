package com.airline.search.service

import com.airline.search.dtos.FlightOption
import com.airline.search.model.FlightRouteDocument
import com.airline.search.repository.FlightRouteRepository
import com.airline.shared.dto.DurationRange
import com.airline.shared.dto.FlightSearchRequest
import com.airline.shared.dto.FlightSearchResponse
import com.airline.shared.dto.FlightSearchResult
import com.airline.shared.dto.FlightSearchSummary
import com.airline.shared.dto.FlightSegment
import com.airline.shared.dto.FlightType
import com.airline.shared.dto.PriceRange
import com.airline.shared.enums.SortOption
import com.airline.shared.exception.SearchServiceException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

/**
 * Flight Search Service - Uses Elasticsearch for fast search
 */
@Service
class FlightSearchService(
    private val flightRouteRepository: FlightRouteRepository
) {
    
    private val logger = LoggerFactory.getLogger(FlightSearchService::class.java)
    
    suspend fun searchFlights(request: FlightSearchRequest): FlightSearchResponse {
        logger.info("Searching flights: ${request.origin} -> ${request.destination} on ${request.departureDate}")
        val startTime = System.currentTimeMillis()
        
        try {
            // Query precomputed route data from Elasticsearch
            val routeDocument = flightRouteRepository
                .findByOriginAndDestinationAndDate(request.origin, request.destination, request.departureDate)
                .awaitSingleOrNull()
            
            val results = if (routeDocument != null) {
                buildSearchResults(routeDocument, request)
            } else {
                logger.warn("No precomputed routes found for ${request.origin} -> ${request.destination} on ${request.departureDate}")
                emptyList()
            }
            
            val searchTimeMs = System.currentTimeMillis() - startTime
            
            return FlightSearchResponse(
                searchId = UUID.randomUUID().toString(),
                results = results,
                totalResults = results.size,
                searchTimeMs = searchTimeMs,
                fromCache = routeDocument != null,
                summary = createSummary(results)
            )
            
        } catch (e: Exception) {
            logger.error("Search failed for ${request.origin} -> ${request.destination}", e)
            throw SearchServiceException(e.localizedMessage)
        }
    }
    
    private fun buildSearchResults(
        routeDocument: FlightRouteDocument, 
        request: FlightSearchRequest
    ): List<FlightSearchResult> {
        val results = mutableListOf<FlightSearchResult>()

        // Add direct flights
        routeDocument.directFlights
            .filter { it.availableSeats >= request.passengers }
            .map { flight ->
                FlightSearchResult(
                    id = "${flight.scheduleId}",
                    type = FlightType.DIRECT,
                    flights = listOf(convertToFlightSegment(flight, request.origin, request.destination)),
                    totalPrice = flight.price * request.passengers,
                    totalDuration = flight.duration,
                    availableSeats = flight.availableSeats,
                    bookingClass = "Economy",
                    fareRules = emptyList()
                )
            }
            .let { results.addAll(it) }
        
        // Add connecting flights if requested
        if (request.includeConnecting) {
            routeDocument.connectingFlights
                .filter { it.minAvailableSeats >= request.passengers }
                .map { connecting ->
                    FlightSearchResult(
                        id = connecting.id,
                        type = FlightType.CONNECTING,
                        flights = connecting.segments.map { segment ->
                            convertToFlightSegment(segment, request.origin, request.destination)
                        },
                        totalPrice = connecting.totalPrice * request.passengers,
                        totalDuration = connecting.totalDuration,
                        availableSeats = connecting.minAvailableSeats,
                        bookingClass = "Economy",
                        fareRules = emptyList()
                    )
                }
                .let { results.addAll(it) }
        }
        
        // Sort and limit results
        return results
            .sortedWith(getSortComparator(request.sortBy))
            .take(request.maxResults)
    }
    
    private fun convertToFlightSegment(
        flight: FlightOption,
        origin: String,
        destination: String
    ): FlightSegment {
        return FlightSegment(
            flightNumber = flight.flightNumber,
            airline = flight.airline,
            origin = origin,
            destination = destination,
            departureDateTime = LocalDateTime.parse(flight.departureTime),
            arrivalDateTime = LocalDateTime.parse(flight.arrivalTime),
            duration = flight.duration,
            aircraft = flight.aircraft,
            price = flight.price
        )
    }
    
    private fun getSortComparator(sortBy: SortOption): Comparator<FlightSearchResult> {
        return when (sortBy) {
            SortOption.PRICE -> compareBy { it.totalPrice }
            SortOption.DURATION -> compareBy { it.totalDuration }
            SortOption.DEPARTURE_TIME -> compareBy { 
                it.flights.first().departureDateTime
            }
            SortOption.ARRIVAL_TIME -> compareBy { 
                it.flights.last().arrivalDateTime
            }
        }
    }
    
    
    private fun createSummary(results: List<FlightSearchResult>): FlightSearchSummary {
        if (results.isEmpty()) {
            return FlightSearchSummary(
                cheapestFlight = null,
                fastestFlight = null,
                priceRange = PriceRange(0.0, 0.0, 0.0),
                durationRange = DurationRange(0, 0, 0),
                availableAirlines = emptyList(),
                directFlightCount = 0,
                connectingFlightCount = 0
            )
        }
        
        val prices = results.map { it.totalPrice }
        val durations = results.map { it.totalDuration }
        val airlines = results.flatMap { result -> result.flights.map { it.airline } }.distinct()
        
        return FlightSearchSummary(
            cheapestFlight = results.minByOrNull { it.totalPrice },
            fastestFlight = results.minByOrNull { it.totalDuration },
            priceRange = PriceRange(
                min = prices.minOrNull() ?: 0.0,
                max = prices.maxOrNull() ?: 0.0,
                average = prices.average()
            ),
            durationRange = DurationRange(
                min = durations.minOrNull() ?: 0,
                max = durations.maxOrNull() ?: 0,
                average = durations.average().toInt()
            ),
            availableAirlines = airlines,
            directFlightCount = results.count { it.type == FlightType.DIRECT },
            connectingFlightCount = results.count { it.type == FlightType.CONNECTING }
        )
    }
}
