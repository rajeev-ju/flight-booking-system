package com.airline.search.repository

import com.airline.search.model.FlightRouteDocument
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDate

@Repository
interface FlightRouteRepository : ReactiveElasticsearchRepository<FlightRouteDocument, String> {

    fun findByOriginAndDestinationAndDate(
        origin: String, 
        destination: String, 
        date: LocalDate
    ): Mono<FlightRouteDocument>

    fun deleteByDateBefore(date: LocalDate): Mono<Void>
}
