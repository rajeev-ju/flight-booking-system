package com.airline.booking.client

import com.airline.booking.dto.FlightScheduleDetails
import com.airline.booking.dto.SeatAvailabilityResponse
import com.airline.booking.exception.FlightServiceException
import com.airline.shared.dto.ApiResponse
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.util.*

@Component
class FlightManagementClient(
    private val webClient: WebClient
) {
    
    @Value("\${services.flight-management.url:http://localhost:8081}")
    private lateinit var flightManagementUrl: String
    
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Check if seats are available for a flight schedule
     */
    suspend fun checkSeatAvailability(
        flightScheduleId: UUID,
        numberOfSeats: Int
    ): SeatAvailabilityResponse {
        return try {
            webClient.get()
                .uri("$flightManagementUrl/api/schedules/$flightScheduleId/seats")
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    response.bodyToMono<String>().flatMap { body ->
                        Mono.error(FlightServiceException("Failed to check seat availability: $body"))
                    }
                }
                .bodyToMono<ApiResponse<Map<String, Any>>>()
                .map { apiResponse ->
                    val data = apiResponse.data ?: throw FlightServiceException("No data in response")
                    val availableSeats = (data["availableSeats"] as? Number)?.toInt()
                        ?: throw FlightServiceException("Invalid availableSeats in response")
                    
                    SeatAvailabilityResponse(
                        available = availableSeats >= numberOfSeats,
                        availableSeats = availableSeats,
                        requestedSeats = numberOfSeats
                    )
                }
                .awaitSingle()
        } catch (e: Exception) {
            logger.error("Error checking seat availability for schedule $flightScheduleId", e)
            throw FlightServiceException("Failed to check seat availability: ${e.message}")
        }
    }
    
    /**
     * Reserve seats for a booking
     */
    suspend fun reserveSeats(
        flightScheduleId: UUID,
        numberOfSeats: Int
    ): Boolean {
        return try {
            val request = SeatOperationRequest(seats = numberOfSeats)
            
            webClient.post()
                .uri("$flightManagementUrl/api/schedules/$flightScheduleId/reserve")
                .bodyValue(request)
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    response.bodyToMono<String>().flatMap { body ->
                        Mono.error(FlightServiceException("Failed to reserve seats: $body"))
                    }
                }
                .bodyToMono<ApiResponse<Any>>()
                .map { apiResponse ->
                    apiResponse.success
                }
                .awaitSingle()
        } catch (e: Exception) {
            logger.error("Error reserving seats for schedule $flightScheduleId", e)
            false
        }
    }
    
    /**
     * Release seats (in case of booking failure)
     */
    suspend fun releaseSeats(
        flightScheduleId: UUID,
        numberOfSeats: Int
    ): Boolean {
        return try {
            val request = SeatOperationRequest(seats = numberOfSeats)
            
            webClient.post()
                .uri("$flightManagementUrl/api/schedules/$flightScheduleId/release")
                .bodyValue(request)
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    response.bodyToMono<String>().flatMap { body ->
                        Mono.error(FlightServiceException("Failed to release seats: $body"))
                    }
                }
                .bodyToMono<Boolean>()
                .awaitSingle()
        } catch (e: Exception) {
            logger.error("Error releasing seats for schedule $flightScheduleId", e)
            false
        }
    }
    
    /**
     * Get flight schedule details
     */
    suspend fun getFlightScheduleDetails(
        flightScheduleId: UUID
    ): FlightScheduleDetails? {
        return try {
            webClient.get()
                .uri("$flightManagementUrl/api/schedules/$flightScheduleId")
                .retrieve()
                .onStatus({ it.isError }) { response ->
                    response.bodyToMono<String>().flatMap { body ->
                        Mono.error(FlightServiceException("Failed to get flight schedule: $body"))
                    }
                }
                .bodyToMono<FlightScheduleDetails>()
                .awaitFirstOrNull()
        } catch (e: Exception) {
            logger.error("Error getting flight schedule details for $flightScheduleId", e)
            null
        }
    }
}

data class SeatOperationRequest(
    val seats: Int
)


