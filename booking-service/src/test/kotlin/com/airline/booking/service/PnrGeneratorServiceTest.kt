package com.airline.booking.service

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PnrGeneratorServiceTest {
    
    private val pnrGeneratorService = PnrGeneratorService()
    
    @Test
    fun `generatePnr should generate 6-character alphanumeric PNR`() {
        val pnr = pnrGeneratorService.generatePnr()
        
        assertEquals(6, pnr.length)
        assertTrue(pnr.matches(Regex("^[A-Z0-9]{6}$")))
    }
    
    @Test
    fun `generatePnr should generate unique PNRs`() {
        val pnrs = mutableSetOf<String>()
        
        repeat(100) {
            pnrs.add(pnrGeneratorService.generatePnr())
        }
        
        // While not guaranteed, 100 random PNRs should be unique
        assertTrue(pnrs.size > 95)
    }
    
    @Test
    fun `isValidPnr should return true for valid PNR format`() {
        assertTrue(pnrGeneratorService.isValidPnr("ABC123"))
        assertTrue(pnrGeneratorService.isValidPnr("XY9Z8K"))
        assertTrue(pnrGeneratorService.isValidPnr("123456"))
        assertTrue(pnrGeneratorService.isValidPnr("ABCDEF"))
    }
    
    @Test
    fun `isValidPnr should return false for invalid PNR format`() {
        // Too short
        assertTrue(!pnrGeneratorService.isValidPnr("ABC12"))
        
        // Too long
        assertTrue(!pnrGeneratorService.isValidPnr("ABC1234"))
        
        // Contains lowercase
        assertTrue(!pnrGeneratorService.isValidPnr("abc123"))
        
        // Contains special characters
        assertTrue(!pnrGeneratorService.isValidPnr("ABC-23"))
        
        // Empty string
        assertTrue(!pnrGeneratorService.isValidPnr(""))
    }
}
