package com.airline.search.validation

import com.airline.shared.dto.FlightSearchRequest
import com.airline.shared.exception.InvalidSearchCriteriaException
import com.airline.shared.validation.Validator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Validates flight search date criteria
 * Single Responsibility: Only handles date validation logic
 */
@Component
class FlightSearchDateValidator : Validator<FlightSearchRequest> {
    
    private val logger = LoggerFactory.getLogger(FlightSearchDateValidator::class.java)
    
    override suspend fun validate(request: FlightSearchRequest) {
        logger.debug("Validating search dates for ${request.origin} -> ${request.destination}")
        
        val today = LocalDate.now()
        val maxAdvanceBooking = today.plusDays(365) // 1 year advance booking
        
        // Validate departure date
        if (request.departureDate.isBefore(today)) {
            throw InvalidSearchCriteriaException("Departure date cannot be in the past")
        }
        
        if (request.departureDate.isAfter(maxAdvanceBooking)) {
            throw InvalidSearchCriteriaException("Departure date cannot be more than 365 days in advance")
        }
        
        // Validate return date if provided
        request.returnDate?.let { returnDate ->
            if (returnDate.isBefore(request.departureDate)) {
                throw InvalidSearchCriteriaException("Return date cannot be before departure date")
            }
            
            if (returnDate.isAfter(maxAdvanceBooking)) {
                throw InvalidSearchCriteriaException("Return date cannot be more than 365 days in advance")
            }
        }
        
        logger.debug("Date validation passed")
    }
}
