package com.airline.search.client

import com.airline.shared.model.Flight
import com.airline.shared.model.FlightSchedule
import com.airline.shared.utils.Constants
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import java.time.LocalDate
import java.util.*

/**
 * Client for communicating with Flight Management Service
 */
@Component
class FlightManagementClient(
    private val webClient: WebClient.Builder,
    @Value("\${services.flight-management.url:http://localhost:8081/flight-management-service}")
    private val flightServiceUrl: String
) {
    
    private val logger = LoggerFactory.getLogger(FlightManagementClient::class.java)
    private val client = webClient.baseUrl(flightServiceUrl).build()
    
    /**
     * Get all active flights from flight management service
     */
    fun getAllActiveFlights(): Flux<Flight> {
        logger.debug("Fetching all active flights from flight management service")
        
        return client.get()
            .uri("/api/flights")
            .header(Constants.X_CORRELATION_ID, UUID.randomUUID().toString())
            .retrieve()
            .bodyToFlux(Flight::class.java)
            .doOnError { error ->
                logger.error("Failed to fetch flights from flight management service", error)
            }
            .onErrorResume { error ->
                logger.warn("Falling back to empty flight list due to error: ${error.message}")
                Flux.empty()
            }
    }
    
    /**
     * Get flight schedules for a specific date
     */
    fun getSchedulesForDate(date: LocalDate): Flux<FlightSchedule> {
        logger.debug("Fetching schedules for date: {} from flight management service", date)
        
        return client.get()
            .uri("/api/schedules") {
                it.queryParam("date", date.toString())
                it.build()
            }
            .header(Constants.X_CORRELATION_ID, UUID.randomUUID().toString())
            .retrieve()
            .bodyToFlux(FlightSchedule::class.java)
            .doOnError { error ->
                logger.error("Failed to fetch schedules for date {} from flight management service", date, error)
            }
            .onErrorResume { error ->
                logger.warn("Falling back to empty schedule list for date {} due to error: {}", date, error.message)
                Flux.empty()
            }
    }
    
    /**
     * Get flight schedules for a date range
     */
    fun getSchedulesForDateRange(startDate: LocalDate, endDate: LocalDate): Flux<FlightSchedule> {
        logger.debug("Fetching schedules for date range: {} to {} from flight management service", startDate, endDate)
        
        return client.get()
            .uri("/api/schedules/range") {
                it.queryParam("startDate", startDate.toString())
                it.queryParam("endDate", endDate.toString())
                it.build()
            }
            .header(Constants.X_CORRELATION_ID, UUID.randomUUID().toString())
            .retrieve()
            .bodyToFlux(FlightSchedule::class.java)
            .doOnError { error ->
                logger.error("Failed to fetch schedules for date range {} to {} from flight management service", 
                    startDate, endDate, error)
            }
            .onErrorResume { error ->
                logger.warn("Falling back to empty schedule list for date range {} to {} due to error: {}", 
                    startDate, endDate, error.message)
                Flux.empty()
            }
    }
    
    /**
     * Get available flights for a specific route and date
     */
    fun getAvailableFlights(
        origin: String,
        destination: String,
        date: LocalDate,
        minSeats: Int = 1
    ): Flux<FlightSchedule> {
        logger.debug("Fetching available flights for route: {} -> {} on {} with min seats: {}", 
            origin, destination, date, minSeats)
        
        return client.get()
            .uri("/api/flights/available") {
                it.queryParam("origin", origin)
                it.queryParam("destination", destination)
                it.queryParam("date", date.toString())
                it.queryParam("minSeats", minSeats)
                it.build()
            }
            .header(Constants.X_CORRELATION_ID, UUID.randomUUID().toString())
            .retrieve()
            .bodyToFlux(FlightSchedule::class.java)
            .doOnError { error ->
                logger.error("Failed to fetch available flights for route {} -> {} on {} from flight management service", 
                    origin, destination, date, error)
            }
            .onErrorResume { error ->
                logger.warn("Falling back to empty available flights list for route {} -> {} on {} due to error: {}", 
                    origin, destination, date, error.message)
                Flux.empty()
            }
    }
}
