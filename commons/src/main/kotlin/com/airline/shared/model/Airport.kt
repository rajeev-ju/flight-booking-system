package com.airline.shared.model

data class Airport(
    val id: Long? = null,
    val code: String, // 3-letter IATA code (e.g., "DEL", "BOM")
    val name: String,
    val city: String,
    val country: String = "India", // Domestic flights only
    val timezone: String,
    val active: Boolean = true
)

data class AirportInfo(
    val code: String,
    val name: String,
    val city: String
)
