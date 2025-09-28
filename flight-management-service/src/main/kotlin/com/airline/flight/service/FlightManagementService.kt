package com.airline.flight.service

import com.airline.flight.contract.request.CreateFlightRequest
import com.airline.flight.contract.request.CreateScheduleRequest
import com.airline.flight.contract.request.UpdateFlightRequest
import com.airline.flight.contract.response.FlightResponse
import com.airline.flight.contract.response.FlightScheduleDetails
import com.airline.flight.contract.response.ScheduleResponse
import com.airline.shared.enums.FlightStatus
import com.airline.shared.exception.FlightNotFoundException
import com.airline.flight.mapper.FlightMapper
import com.airline.flight.mapper.ScheduleMapper
import com.airline.flight.repository.FlightRepository
import com.airline.flight.repository.FlightScheduleEntityRepository
import com.airline.shared.dto.FlightScheduleWithDetails
import com.airline.shared.model.Flight
import com.airline.shared.model.FlightSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

@Service
class FlightManagementService(
    private val flightRepository: FlightRepository,
    private val scheduleRepository: FlightScheduleEntityRepository,
    private val flightMapper: FlightMapper,
    private val scheduleMapper: ScheduleMapper
) {
    
    private val logger = LoggerFactory.getLogger(FlightManagementService::class.java)

    fun getAllActiveFlights(): Flow<Flight> {
        logger.debug("Fetching all active flights")
        return flightRepository.findAllActive()
            .map { entity ->
                Flight(
                    id = entity.id,
                    flightNumber = entity.flightNumber,
                    airlineCode = "AI", // TODO: Get from route/airline join
                    originAirportCode = "DEL", // TODO: Get from route join
                    destinationAirportCode = "BOM", // TODO: Get from route join
                    departureTime = entity.departureTime,
                    arrivalTime = entity.arrivalTime,
                    duration = entity.durationMinutes,
                    aircraft = entity.aircraftType,
                    totalSeats = entity.totalSeats,
                    basePrice = entity.price,
                    active = entity.active,
                    effectiveFrom = entity.createdAt,
                    effectiveTo = entity.createdAt.plusYears(1)
                )
            }
    }

    @Transactional
    suspend fun createFlight(request: CreateFlightRequest): FlightResponse {
        logger.info("Creating new flight: ${request.flightNumber}")

        val existingFlight = flightRepository.findByFlightNumber(request.flightNumber)
        if (existingFlight != null) {
            throw IllegalArgumentException("Flight number ${request.flightNumber} already exists")
        }
        
        val entity = flightMapper.toEntity(request)
        val savedEntity = flightRepository.save(entity)
        
        logger.info("Successfully created flight: ${savedEntity.flightNumber} with ID: ${savedEntity.id}")
        return flightMapper.toResponse(savedEntity)
    }
    
    /**
     * Update flight details
     */
    @Transactional
    suspend fun updateFlight(flightId: UUID, request: UpdateFlightRequest): FlightResponse {
        logger.info("Updating flight: $flightId")
        
        val existingFlight = flightRepository.findById(flightId)
            ?: throw FlightNotFoundException("Flight with id: $flightId doesn't exist")

        val updatedEntity = flightMapper.updateEntity(existingFlight, request)
        val savedEntity = flightRepository.save(updatedEntity)
        
        logger.info("Successfully updated flight: ${savedEntity.flightNumber}")
        return flightMapper.toResponse(savedEntity)
    }

    suspend fun getFlightById(flightId: UUID): FlightResponse {
        logger.debug("Fetching flight by ID: {}", flightId)
        return flightMapper.toResponse(
            flightRepository.findById(flightId)
                ?: throw FlightNotFoundException("Flight with id: $flightId doesn't exist")
        )
    }

    fun getFlightsByRoute(origin: String, destination: String): Flow<FlightResponse> {
        logger.debug("Fetching flights for route: $origin -> $destination")
        return flightRepository.findByRoute(origin, destination)
            .map { flightMapper.toResponse(it) }
    }

    fun getSchedulesForDate(date: LocalDate): Flow<FlightSchedule> {
        logger.debug("Fetching schedules for date: {}", date)
        return scheduleRepository.findByDate(date)
            .map { scheduleMapper.toSharedModel(it) }
    }

    fun getSchedulesForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<FlightSchedule> {
        logger.debug("Fetching schedules for date range: $startDate to $endDate")
        return scheduleRepository.findByDateRange(startDate, endDate)
            .map { scheduleMapper.toSharedModel(it) }
    }

    @Transactional
    suspend fun createSchedule(request: CreateScheduleRequest): ScheduleResponse {
        logger.info("Creating schedule for flight: ${request.flightNumber} on ${request.scheduleDate}")
        
        val entity = scheduleMapper.toEntity(request)
        val savedEntity = scheduleRepository.save(entity)
        
        logger.info("Successfully created schedule with ID: ${savedEntity.id}")
        return scheduleMapper.toResponse(savedEntity)
    }

    fun getAvailableFlights(
        origin: String,
        destination: String,
        date: LocalDate,
        minSeats: Int
    ): Flow<FlightSchedule> {
        logger.debug("Fetching available flights: $origin -> $destination on $date, minSeats: $minSeats")
        return scheduleRepository.findAvailableFlights(origin, destination, date, minSeats)
            .map { scheduleMapper.toSharedModel(it) }
    }

    @Transactional
    suspend fun reserveSeats(scheduleId: UUID, seats: Int): Boolean {
        logger.info("Reserving $seats seats for schedule: $scheduleId")
        
        val updatedRows = scheduleRepository.reserveSeats(scheduleId, seats)
        val success = updatedRows > 0
        
        if (success) {
            logger.info("Successfully reserved $seats seats for schedule: $scheduleId")
        } else {
            logger.warn("Failed to reserve $seats seats for schedule: $scheduleId - insufficient availability or invalid schedule")
        }
        
        return success
    }

    @Transactional
    suspend fun releaseSeats(scheduleId: UUID, seats: Int): Boolean {
        logger.info("Releasing $seats seats for schedule: $scheduleId")
        
        val updatedRows = scheduleRepository.releaseSeats(scheduleId, seats)
        val success = updatedRows > 0
        
        if (success) {
            logger.info("Successfully released $seats seats for schedule: $scheduleId")
        } else {
            logger.warn("Failed to release $seats seats for schedule: $scheduleId")
        }
        
        return success
    }

    @Transactional
    suspend fun updateFlightStatus(scheduleId: UUID, status: FlightStatus): Boolean {
        logger.info("Updating flight status to $status for schedule: $scheduleId")
        
        val updatedRows = scheduleRepository.updateStatus(scheduleId, status)
        val success = updatedRows > 0
        
        if (success) {
            logger.info("Successfully updated flight status to $status for schedule: $scheduleId")
        } else {
            logger.warn("Failed to update flight status for schedule: $scheduleId")
        }
        
        return success
    }

    suspend fun getAvailableSeats(scheduleId: UUID): Int {
        logger.debug("Checking available seats for schedule: {}", scheduleId)
        return scheduleRepository.getAvailableSeats(scheduleId)
    }

    suspend fun getScheduleDetailsForBooking(scheduleId: UUID): FlightScheduleDetails {
        logger.debug("Fetching schedule details for booking service, ID: {}", scheduleId)

        val scheduleWithDetails = getScheduleWithDetails(scheduleId)
        return scheduleMapper.toFlightScheduleDetails(scheduleWithDetails)
    }


    private suspend fun getScheduleWithDetails(scheduleId: UUID): FlightScheduleWithDetails {
        logger.debug("Fetching schedule details for ID: {}", scheduleId)

        val scheduleDetails = scheduleRepository.findScheduleWithCompleteDetails(scheduleId)
            ?: throw FlightNotFoundException("Flight schedule not found with ID: $scheduleId")

        return FlightScheduleWithDetails(
            id = scheduleDetails.id,
            flightNumber = scheduleDetails.flightNumber,
            origin = scheduleDetails.originCity,
            destination = scheduleDetails.destinationCity,
            departureDateTime = scheduleDetails.departureDateTime,
            arrivalDateTime = scheduleDetails.arrivalDateTime,
            availableSeats = scheduleDetails.availableSeats,
            totalSeats = scheduleDetails.totalSeats,
            price = scheduleDetails.price,
            status = scheduleDetails.status
        )
    }
}
