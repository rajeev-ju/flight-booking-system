package com.airline.shared.utils

class AirlineUtils {
    companion object {
        fun getAirlineName(code: String) = when (code) {
            "AI" -> "Air India"
            "6E" -> "IndiGo"
            "SG" -> "SpiceJet"
            "UK" -> "Vistara"
            "G8" -> "GoAir"
            else -> "Unknown Airline"
        }
    }
}