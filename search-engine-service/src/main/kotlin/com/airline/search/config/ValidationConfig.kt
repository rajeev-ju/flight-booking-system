package com.airline.search.config

import com.airline.search.validation.FlightSearchDateValidator
import com.airline.search.validation.FlightRouteValidator
import com.airline.search.validation.PassengerValidator
import com.airline.search.validation.SearchParametersValidator
import com.airline.shared.dto.FlightSearchRequest
import com.airline.shared.validation.ValidationHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Validation configuration following Dependency Inversion Principle
 * Configures the validation chain for flight search requests
 */
@Configuration
class ValidationConfig {

    @Bean
    fun flightSearchValidationHandler(
        dateValidator: FlightSearchDateValidator,
        routeValidator: FlightRouteValidator,
        passengerValidator: PassengerValidator,
        parametersValidator: SearchParametersValidator
    ): ValidationHandler<FlightSearchRequest> {
        
        return ValidationHandler(
            validators = listOf(
                dateValidator,
                routeValidator,
                passengerValidator,
                parametersValidator
            )
        )
    }
}
