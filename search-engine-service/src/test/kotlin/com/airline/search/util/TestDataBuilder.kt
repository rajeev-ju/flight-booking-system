package com.airline.search.util

import com.airline.search.dtos.ConnectingFlightOption
import com.airline.search.dtos.FlightOption
import com.airline.search.model.FlightRouteDocument
import com.airline.shared.dto.DurationRange
import com.airline.shared.dto.FlightSearchRequest
import com.airline.shared.dto.FlightSearchResponse
import com.airline.shared.dto.FlightSearchResult
import com.airline.shared.dto.FlightSearchSummary
import com.airline.shared.dto.FlightSegment
import com.airline.shared.dto.FlightType
import com.airline.shared.dto.PriceRange
import com.airline.shared.enums.CabinClass
import com.airline.shared.enums.FlightStatus
import com.airline.shared.enums.SortOption
import com.airline.shared.model.Flight
import com.airline.shared.model.FlightSchedule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

object TestDataBuilder {

    fun buildFlightSearchRequest(
        origin: String = "DEL",
        destination: String = "BOM",
        departureDate: LocalDate = LocalDate.now().plusDays(7),
        passengers: Int = 1,
        returnDate: LocalDate? = null,
        sortBy: SortOption = SortOption.PRICE,
        maxResults: Int = 20,
        includeConnecting: Boolean = true,
        maxStops: Int = 1,
        cabinClass: CabinClass = CabinClass.ECONOMY
    ) = FlightSearchRequest(
        origin = origin,
        destination = destination,
        departureDate = departureDate,
        passengers = passengers,
        returnDate = returnDate,
        sortBy = sortBy,
        maxResults = maxResults,
        includeConnecting = includeConnecting,
        maxStops = maxStops,
        cabinClass = cabinClass
    )

    fun buildFlightSearchResponse(
        searchId: String = UUID.randomUUID().toString(),
        results: List<FlightSearchResult> = listOf(buildFlightSearchResult()),
        totalResults: Int = results.size,
        searchTimeMs: Long = 150,
        fromCache: Boolean = true,
        summary: FlightSearchSummary = buildFlightSearchSummary()
    ) = FlightSearchResponse(
        searchId = searchId,
        results = results,
        totalResults = totalResults,
        searchTimeMs = searchTimeMs,
        fromCache = fromCache,
        summary = summary
    )

    fun buildFlightSearchResult(
        id: String = "FLIGHT_001",
        flights: List<FlightSegment> = listOf(buildFlightSegment()),
        totalPrice: Double = 5000.0,
        totalDuration: Int = 150,
        type: FlightType = FlightType.DIRECT,
        availableSeats: Int = 50,
        bookingClass: String = "Economy",
        fareRules: List<String> = emptyList()
    ) = FlightSearchResult(
        id = id,
        flights = flights,
        totalPrice = totalPrice,
        totalDuration = totalDuration,
        type = type,
        availableSeats = availableSeats,
        bookingClass = bookingClass,
        fareRules = fareRules
    )

    fun buildFlightSegment(
        flightNumber: String = "AI101",
        airline: String = "AI",
        origin: String = "DEL",
        destination: String = "BOM",
        departureDateTime: LocalDateTime = LocalDateTime.of(2025, 10, 5, 6, 0),
        arrivalDateTime: LocalDateTime = LocalDateTime.of(2025, 10, 5, 8, 30),
        duration: Int = 150,
        aircraft: String = "Boeing 737",
        price: Double = 5000.0
    ) = FlightSegment(
        flightNumber = flightNumber,
        airline = airline,
        origin = origin,
        destination = destination,
        departureDateTime = departureDateTime,
        arrivalDateTime = arrivalDateTime,
        duration = duration,
        aircraft = aircraft,
        price = price
    )

    fun buildFlightSearchSummary(
        cheapestFlight: FlightSearchResult? = buildFlightSearchResult(totalPrice = 4000.0),
        fastestFlight: FlightSearchResult? = buildFlightSearchResult(totalDuration = 90),
        priceRange: PriceRange = PriceRange(4000.0, 8000.0, 6000.0),
        durationRange: DurationRange = DurationRange(90, 210, 150),
        availableAirlines: List<String> = listOf("AI", "6E", "SG"),
        directFlightCount: Int = 3,
        connectingFlightCount: Int = 2
    ) = FlightSearchSummary(
        cheapestFlight = cheapestFlight,
        fastestFlight = fastestFlight,
        priceRange = priceRange,
        durationRange = durationRange,
        availableAirlines = availableAirlines,
        directFlightCount = directFlightCount,
        connectingFlightCount = connectingFlightCount
    )

    fun buildFlightRouteDocument(
        id: String = "DEL_BOM_2025-10-05",
        origin: String = "DEL",
        destination: String = "BOM",
        date: LocalDate = LocalDate.of(2025, 10, 5),
        directFlights: List<FlightOption> = listOf(buildFlightOption()),
        connectingFlights: List<ConnectingFlightOption> = listOf(buildConnectingFlightOption()),
        minPrice: Double = 4000.0,
        minDuration: Int = 90,
        lastUpdated: Long = System.currentTimeMillis()
    ) = FlightRouteDocument(
        id = id,
        origin = origin,
        destination = destination,
        date = date,
        directFlights = directFlights,
        connectingFlights = connectingFlights,
        minPrice = minPrice,
        minDuration = minDuration,
        lastUpdated = lastUpdated
    )

    fun buildFlightOption(
        scheduleId: UUID = UUID.randomUUID(),
        flightNumber: String = "AI101",
        airline: String = "AI",
        departureTime: String = "2025-10-05T06:00:00",
        arrivalTime: String = "2025-10-05T08:30:00",
        duration: Int = 150,
        price: Double = 5000.0,
        availableSeats: Int = 50,
        aircraft: String = "Boeing 737"
    ) = FlightOption(
        scheduleId = scheduleId,
        flightNumber = flightNumber,
        airline = airline,
        departureTime = departureTime,
        arrivalTime = arrivalTime,
        duration = duration,
        price = price,
        availableSeats = availableSeats,
        aircraft = aircraft
    )

    fun buildConnectingFlightOption(
        id: String = "CON_001",
        segments: List<FlightOption> = listOf(
            buildFlightOption(
                flightNumber = "6E201",
                airline = "6E",
                departureTime = "2025-10-05T07:00:00",
                arrivalTime = "2025-10-05T09:00:00",
                duration = 120,
                price = 3000.0
            ),
            buildFlightOption(
                flightNumber = "6E202",
                airline = "6E",
                departureTime = "2025-10-05T10:00:00",
                arrivalTime = "2025-10-05T11:30:00",
                duration = 90,
                price = 2500.0
            )
        ),
        totalPrice: Double = 5500.0,
        totalDuration: Int = 270,
        minAvailableSeats: Int = 25,
        layoverAirport: String = "BLR",
        layoverDuration: Int = 60
    ) = ConnectingFlightOption(
        id = id,
        segments = segments,
        totalPrice = totalPrice,
        totalDuration = totalDuration,
        minAvailableSeats = minAvailableSeats,
        layoverAirport = layoverAirport,
        layoverDuration = layoverDuration
    )

    fun buildFlight(
        id: UUID = UUID.randomUUID(),
        flightNumber: String = "AI101",
        airlineCode: String = "AI",
        originAirportCode: String = "DEL",
        destinationAirportCode: String = "BOM",
        departureTime: LocalTime = LocalTime.of(6, 0),
        arrivalTime: LocalTime = LocalTime.of(8, 30),
        duration: Int = 150,
        aircraft: String = "Boeing 737",
        totalSeats: Int = 180,
        basePrice: Double = 5000.0,
        active: Boolean = true,
        effectiveFrom: LocalDateTime = LocalDateTime.now(),
        effectiveTo: LocalDateTime = LocalDateTime.now().plusYears(1)
    ) = Flight(
        id = id,
        flightNumber = flightNumber,
        airlineCode = airlineCode,
        originAirportCode = originAirportCode,
        destinationAirportCode = destinationAirportCode,
        departureTime = departureTime,
        arrivalTime = arrivalTime,
        duration = duration,
        aircraft = aircraft,
        totalSeats = totalSeats,
        basePrice = basePrice,
        active = active,
        effectiveFrom = effectiveFrom,
        effectiveTo = effectiveTo
    )

    fun buildFlightSchedule(
        id: UUID = UUID.randomUUID(),
        flightNumber: String = "AI101",
        scheduleDate: LocalDateTime = LocalDateTime.of(2025, 10, 5, 6, 0),
        departureDateTime: LocalDateTime = LocalDateTime.of(2025, 10, 5, 6, 0),
        arrivalDateTime: LocalDateTime = LocalDateTime.of(2025, 10, 5, 8, 30),
        availableSeats: Int = 100,
        price: Double = 5000.0,
        status: FlightStatus = FlightStatus.SCHEDULED
    ) = FlightSchedule(
        id = id,
        flightNumber = flightNumber,
        scheduleDate = scheduleDate,
        departureDateTime = departureDateTime,
        arrivalDateTime = arrivalDateTime,
        availableSeats = availableSeats,
        price = price,
        status = status
    )

    // Helper methods for creating multiple test data items
    fun buildMultipleFlightOptions(count: Int, basePrice: Double = 5000.0): List<FlightOption> {
        return (1..count).map { i ->
            buildFlightOption(
                flightNumber = "AI10$i",
                price = basePrice + (i * 100),
                availableSeats = 50 - (i * 5),
                departureTime = "2025-10-05T${String.format("%02d", 5 + i)}:00:00",
                arrivalTime = "2025-10-05T${String.format("%02d", 7 + i)}:30:00"
            )
        }
    }

    fun buildMultipleFlightSearchResults(count: Int): List<FlightSearchResult> {
        return (1..count).map { i ->
            buildFlightSearchResult(
                id = "FLIGHT_00$i",
                totalPrice = 4000.0 + (i * 500),
                totalDuration = 90 + (i * 30),
                availableSeats = 60 - (i * 10)
            )
        }
    }

    fun buildRouteDocumentWithNoFlights() = FlightRouteDocument(
        id = "DEL_BOM_2025-10-05",
        origin = "DEL",
        destination = "BOM",
        date = LocalDate.of(2025, 10, 5),
        directFlights = emptyList(),
        connectingFlights = emptyList(),
        minPrice = 0.0,
        minDuration = 0
    )

    fun buildRouteDocumentWithOnlyDirectFlights(count: Int = 3) = FlightRouteDocument(
        id = "DEL_BOM_2025-10-05",
        origin = "DEL",
        destination = "BOM",
        date = LocalDate.of(2025, 10, 5),
        directFlights = buildMultipleFlightOptions(count),
        connectingFlights = emptyList(),
        minPrice = 5000.0,
        minDuration = 150
    )

    fun buildRouteDocumentWithOnlyConnectingFlights(count: Int = 2) = FlightRouteDocument(
        id = "DEL_BOM_2025-10-05",
        origin = "DEL",
        destination = "BOM",
        date = LocalDate.of(2025, 10, 5),
        directFlights = emptyList(),
        connectingFlights = (1..count).map { i ->
            buildConnectingFlightOption(
                id = "CON_00$i",
                totalPrice = 5000.0 + (i * 500),
                totalDuration = 200 + (i * 30)
            )
        },
        minPrice = 5000.0,
        minDuration = 200
    )
}
