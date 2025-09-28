package com.airline.booking.service

import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class PnrGeneratorService {
    
    companion object {
        private const val PNR_LENGTH = 6
        private val CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }
    
    /**
     * Generates a unique 6-character alphanumeric PNR
     * Format: XXXXXX (e.g., ABC123, XY9Z8K)
     */
    fun generatePnr(): String {
        return buildString {
            repeat(PNR_LENGTH) {
                append(CHARS[Random.nextInt(CHARS.length)])
            }
        }
    }

    fun isValidPnr(pnr: String): Boolean {
        return pnr.matches(Regex("^[A-Z0-9]{$PNR_LENGTH}$"))
    }
}
