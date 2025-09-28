package com.airline.search.service

import com.airline.search.client.FlightManagementClient
import com.airline.search.dtos.ConnectingFlightOption
import com.airline.search.dtos.FlightOption
import com.airline.search.model.FlightRouteDocument
import com.airline.search.repository.FlightRouteRepository
import com.airline.shared.model.Flight
import com.airline.shared.model.FlightSchedule
import com.airline.shared.utils.AirlineUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class RoutePrecomputeService(
    private val flightRouteRepository: FlightRouteRepository,
    private val flightManagementClient: FlightManagementClient
) {
    private val logger = LoggerFactory.getLogger(RoutePrecomputeService::class.java)
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Scheduled wrapper for precomputeRoutes
     * Spring requires a no-arg method for @Scheduled
     */
    @Scheduled(fixedRate = 1 * 60 * 1000) // 1 minute
    fun precomputeRoutesJob() {
        scope.launch {
            precomputeRoutes()
        }
    }

    /**
     * Actual suspend function for route precomputation
     */
    private suspend fun precomputeRoutes() {
        logger.info("Starting route precomputation job")
        val startTime = System.currentTimeMillis()
        try {
            val today = LocalDate.now()
            val endDate = today.plusDays(7)
            var processedRoutes = 0
            var currentDate = today

            while (!currentDate.isAfter(endDate)) {
                val routesForDate = precomputeRoutesForDate(currentDate)
                processedRoutes += routesForDate
                currentDate = currentDate.plusDays(1)
            }

            cleanupOldData(today.minusDays(1))

            val duration = System.currentTimeMillis() - startTime
            logger.info("Route precomputation completed. Processed $processedRoutes routes in ${duration}ms")
        } catch (e: Exception) {
            logger.error("Error during route precomputation", e)
        }
    }

    private suspend fun precomputeRoutesForDate(date: LocalDate): Int {
        logger.debug("Precomputing routes for date: {}", date)
        val flights = getFlightsFromService()
        val schedules = getSchedulesForDate(date)
        val schedulesByFlight = schedules.groupBy { it.flightNumber }
        val airportPairs = getUniqueAirportPairs(flights)
        var processedRoutes = 0

        for ((origin, destination) in airportPairs) {
            try {
                val routeDocument = computeRouteForPair(origin, destination, date, flights, schedulesByFlight)
                if (routeDocument != null) {
                    flightRouteRepository.save(routeDocument).awaitSingle()
                    processedRoutes++
                }
            } catch (e: Exception) {
                logger.error("Error computing route $origin -> $destination for $date", e)
            }
        }
        return processedRoutes
    }

    private suspend fun computeRouteForPair(
        origin: String,
        destination: String,
        date: LocalDate,
        allFlights: List<Flight>,
        schedulesByFlight: Map<String, List<FlightSchedule>>
    ): FlightRouteDocument? {
        val directFlights = mutableListOf<FlightOption>()
        val connectingFlights = mutableListOf<ConnectingFlightOption>()

        allFlights
            .filter { it.originAirportCode == origin && it.destinationAirportCode == destination }
            .forEach { flight ->
                schedulesByFlight[flight.flightNumber]?.forEach { schedule ->
                    if (schedule.availableSeats > 0) {
                        directFlights.add(createFlightOption(flight, schedule))
                    }
                }
            }

        connectingFlights.addAll(findConnectingFlights(origin, destination, allFlights, schedulesByFlight))

        if (directFlights.isEmpty() && connectingFlights.isEmpty()) return null

        val minPrice = (directFlights.minOfOrNull { it.price } ?: Double.MAX_VALUE)
            .coerceAtMost(connectingFlights.minOfOrNull { it.totalPrice } ?: Double.MAX_VALUE)
        val minDuration = (directFlights.minOfOrNull { it.duration } ?: Int.MAX_VALUE)
            .coerceAtMost(connectingFlights.minOfOrNull { it.totalDuration } ?: Int.MAX_VALUE)

        return FlightRouteDocument(
            id = "${origin}_${destination}_${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
            origin = origin,
            destination = destination,
            date = date,
            directFlights = directFlights,
            connectingFlights = connectingFlights,
            minPrice = minPrice,
            minDuration = minDuration
        )
    }

    private fun findConnectingFlights(
        origin: String,
        destination: String,
        allFlights: List<Flight>,
        schedulesByFlight: Map<String, List<FlightSchedule>>
    ): List<ConnectingFlightOption> {
        val connectingFlights = mutableListOf<ConnectingFlightOption>()
        val firstLegFlights = allFlights.filter { it.originAirportCode == origin }
        val secondLegFlights = allFlights.filter { it.destinationAirportCode == destination }

        val layoverAirports = firstLegFlights
            .map { it.destinationAirportCode }
            .intersect(secondLegFlights.map { it.originAirportCode }.toSet())

        for (layover in layoverAirports) {
            if (layover == origin || layover == destination) continue
            val firstLeg = firstLegFlights.filter { it.destinationAirportCode == layover }
            val secondLeg = secondLegFlights.filter { it.originAirportCode == layover }

            for (flight1 in firstLeg) {
                for (flight2 in secondLeg) {
                    val schedules1 = schedulesByFlight[flight1.flightNumber] ?: continue
                    val schedules2 = schedulesByFlight[flight2.flightNumber] ?: continue
                    for (s1 in schedules1) {
                        for (s2 in schedules2) {
                            val layoverMinutes = java.time.Duration.between(s1.arrivalDateTime, s2.departureDateTime).toMinutes()
                            if (layoverMinutes in 60..240 && s1.availableSeats > 0 && s2.availableSeats > 0) {
                                connectingFlights.add(
                                    ConnectingFlightOption(
                                        id = "${s1.id}_${s2.id}",
                                        segments = listOf(createFlightOption(flight1, s1), createFlightOption(flight2, s2)),
                                        layoverAirport = layover,
                                        layoverDuration = layoverMinutes.toInt(),
                                        totalDuration = createFlightOption(flight1, s1).duration + createFlightOption(flight2, s2).duration + layoverMinutes.toInt(),
                                        totalPrice = createFlightOption(flight1, s1).price + createFlightOption(flight2, s2).price,
                                        minAvailableSeats = minOf(createFlightOption(flight1, s1).availableSeats, createFlightOption(flight2, s2).availableSeats)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        return connectingFlights
    }

    private fun createFlightOption(flight: Flight, schedule: FlightSchedule) = FlightOption(
        scheduleId = schedule.id,
        flightNumber = flight.flightNumber,
        airline = AirlineUtils.getAirlineName(flight.airlineCode),
        departureTime = schedule.departureDateTime.toString(),
        arrivalTime = schedule.arrivalDateTime.toString(),
        duration = flight.duration,
        price = schedule.price,
        availableSeats = schedule.availableSeats,
        aircraft = flight.aircraft
    )

    private fun getUniqueAirportPairs(flights: List<Flight>) = mutableSetOf<Pair<String, String>>().apply {
        flights.forEach { add(Pair(it.originAirportCode, it.destinationAirportCode)) }
        val airports = flights.flatMap { listOf(it.originAirportCode, it.destinationAirportCode) }.toSet()
        for (origin in airports) {
            for (destination in airports) if (origin != destination) add(Pair(origin, destination))
        }
    }

    private suspend fun getFlightsFromService(): List<Flight> {
        return try {
            flightManagementClient.getAllActiveFlights()
                .collectList()
                .awaitSingleOrNull() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Failed to fetch flights from flight management service", e)
            emptyList()
        }
    }

    private suspend fun getSchedulesForDate(date: LocalDate): List<FlightSchedule> {
        return try {
            flightManagementClient.getSchedulesForDate(date)
                .collectList()
                .awaitSingleOrNull() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Failed to fetch schedules for date: {}", date, e)
            emptyList()
        }
    }

    private suspend fun cleanupOldData(beforeDate: LocalDate) {
        try {
            flightRouteRepository.deleteByDateBefore(beforeDate).awaitSingle()
            logger.info("Cleaned up route data before $beforeDate")
        } catch (e: Exception) {
            logger.error("Failed to cleanup old route data", e)
        }
    }
}
