package com.airline.search.validation

import com.airline.shared.dto.FlightSearchRequest
import com.airline.shared.exception.ValidationException
import com.airline.shared.validation.Validator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Validates passenger count and constraints
 * Single Responsibility: Only handles passenger validation logic
 */
@Component
class PassengerValidator : Validator<FlightSearchRequest> {
    
    private val logger = LoggerFactory.getLogger(PassengerValidator::class.java)
    
    override suspend fun validate(request: FlightSearchRequest) {
        logger.debug("Validating passenger count: ${request.passengers}")
        
        if (request.passengers < 1) {
            throw ValidationException(
                message = "At least 1 passenger is required"
            )
        }
        
        if (request.passengers > 9) {
            throw ValidationException(
                message = "Maximum 9 passengers allowed per booking"
            )
        }
        
        logger.debug("Passenger validation passed")
    }
}
