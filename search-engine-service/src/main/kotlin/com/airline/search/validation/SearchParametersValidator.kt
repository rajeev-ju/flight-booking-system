package com.airline.search.validation

import com.airline.shared.dto.FlightSearchRequest
import com.airline.shared.exception.ValidationException
import com.airline.shared.validation.Validator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Validates search parameters and constraints
 * Single Responsibility: Only handles search parameter validation logic
 */
@Component
class SearchParametersValidator : Validator<FlightSearchRequest> {
    
    private val logger = LoggerFactory.getLogger(SearchParametersValidator::class.java)
    
    override suspend fun validate(request: FlightSearchRequest) {
        logger.debug("Validating search parameters: maxResults=${request.maxResults}, maxStops=${request.maxStops}")
        
        if (request.maxResults < 1 || request.maxResults > 100) {
            throw ValidationException(
                message = "Max results must be between 1 and 100"
            )
        }
        
        if (request.maxStops < 0 || request.maxStops > 2) {
            throw ValidationException(
                message = "Max stops must be between 0 and 2"
            )
        }
        
        logger.debug("Search parameters validation passed")
    }
}
