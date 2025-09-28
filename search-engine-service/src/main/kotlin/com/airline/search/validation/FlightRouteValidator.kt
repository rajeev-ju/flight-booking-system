package com.airline.search.validation

import com.airline.shared.dto.FlightSearchRequest
import com.airline.shared.exception.InvalidSearchCriteriaException
import com.airline.shared.validation.Validator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Validates flight route criteria
 * Single Responsibility: Only handles route validation logic
 */
@Component
class FlightRouteValidator : Validator<FlightSearchRequest> {
    
    private val logger = LoggerFactory.getLogger(FlightRouteValidator::class.java)
    
    // In production, this would come from a database or external service
    private val supportedAirports = setOf(
        "DEL", "BOM", "BLR", "MAA", "CCU", "HYD", "PNQ", "AMD", "GOI", "COK",
        "JAI", "LKO", "PAT", "BHU", "IXC", "GAU", "IXR", "RPR", "IDR", "NAG",
        "VNS", "IXJ", "SXR", "IXL", "ATQ", "IXD", "AGR", "KNU", "GWL", "JLR",
        "BHO", "UDR", "JDH", "BKB", "KTU", "HSS", "PGH", "DED", "IXS", "IMF",
        "AJL", "AGX", "IXZ", "TCR", "TRZ", "CJB", "MDU", "STV", "RAJ", "BDQ"
    )
    
    override suspend fun validate(request: FlightSearchRequest) {
        logger.debug("Validating route: ${request.origin} -> ${request.destination}")
        
        // Validate origin airport
        if (!supportedAirports.contains(request.origin)) {
            throw InvalidSearchCriteriaException("Origin airport not supported")
        }
        
        // Validate destination airport
        if (!supportedAirports.contains(request.destination)) {
            throw InvalidSearchCriteriaException("Destination airport not supported"
            )
        }
        
        // Validate origin != destination
        if (request.origin == request.destination) {
            throw InvalidSearchCriteriaException("Origin and destination cannot be the same")
        }
        
        logger.debug("Route validation passed")
    }
}
