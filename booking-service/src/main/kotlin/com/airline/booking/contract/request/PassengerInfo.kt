package com.airline.booking.contract.request

import com.airline.booking.enums.Gender
import com.airline.booking.enums.IdType
import jakarta.validation.constraints.*

data class PassengerInfo(
    @field:NotBlank(message = "First name is required")
    @field:Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    val firstName: String,
    
    @field:NotBlank(message = "Last name is required")
    @field:Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    val lastName: String,
    
    @field:Min(value = 1, message = "Age must be at least 1")
    @field:Max(value = 120, message = "Age must be less than 120")
    val age: Int,
    
    @field:NotNull(message = "Gender is required")
    val gender: Gender,
    
    @field:NotNull(message = "ID type is required")
    val idType: IdType,
    
    @field:NotBlank(message = "ID number is required")
    @field:Pattern(
        regexp = "^[A-Z0-9]{6,20}$",
        message = "ID number must be 6-20 alphanumeric characters"
    )
    val idNumber: String
)
