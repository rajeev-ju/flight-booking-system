package com.airline.shared.validation

/**
 * Generic validator interface following SOLID principles
 * Enables composable validation with single responsibility
 */
interface Validator<T> {
    /**
     * Validates the given request
     * @param request The request to validate
     * @throws ValidationException if validation fails
     */
    suspend fun validate(request: T)
}

/**
 * Validation handler that composes multiple validators
 * Follows Open/Closed Principle - open for extension, closed for modification
 */
class ValidationHandler<T>(
    private val validators: List<Validator<T>>
) {
    suspend fun validate(request: T) {
        validators.forEach { validator ->
            validator.validate(request)
        }
    }
}
