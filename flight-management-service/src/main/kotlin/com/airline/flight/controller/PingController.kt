package com.airline.flight.controller

import com.airline.flight.contract.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

/**
 * Simple ping controller for health checks
 */
@RestController
class PingController {
    
    @GetMapping("/ping")
    fun ping(
        @RequestHeader(value = "X-Request-ID", required = false) requestId: String?
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        val response = mapOf(
            "status" to "UP",
            "message" to "pong",
            "service" to "flight-management-service",
            "timestamp" to java.time.Instant.now().toString()
        )
        
        return ResponseEntity.ok(ApiResponse.success(response, "Service is healthy", requestId))
    }
}
