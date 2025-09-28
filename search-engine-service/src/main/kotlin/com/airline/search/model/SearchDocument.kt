package com.airline.search.model

import com.airline.search.dtos.ConnectingFlightOption
import com.airline.search.dtos.FlightOption
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDate

/**
 * Elasticsearch document for precomputed route data
 * This contains all possible flight combinations for fast search
 */
@Document(indexName = "flight_routes")
data class FlightRouteDocument(
    @Id
    val id: String, // Format: "origin_destination_date"

    @Field(type = FieldType.Keyword)
    val origin: String,

    @Field(type = FieldType.Keyword) 
    val destination: String,

    @Field(type = FieldType.Date)
    val date: LocalDate,

    @Field(type = FieldType.Nested)
    val directFlights: List<FlightOption>,

    @Field(type = FieldType.Nested)
    val connectingFlights: List<ConnectingFlightOption>,

    @Field(type = FieldType.Double)
    val minPrice: Double,

    @Field(type = FieldType.Integer)
    val minDuration: Int, // in minutes

    @Field(type = FieldType.Long)
    val lastUpdated: Long = System.currentTimeMillis()
)
