package com.airline.migration.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType

@Service
class ElasticsearchMigrationService {

    private val logger = LoggerFactory.getLogger(ElasticsearchMigrationService::class.java)

    @Value("\${migration.elasticsearch.enabled:true}")
    private var migrationEnabled: Boolean = true

    @Value("\${migration.elasticsearch.recreate-indices:false}")
    private var recreateIndices: Boolean = false

    @Value("\${spring.elasticsearch.uris:http://localhost:9200}")
    private lateinit var elasticsearchUris: String

    @Value("\${migration.elasticsearch.indices.flight-routes:flight-routes-v1}")
    private lateinit var flightRoutesIndex: String

    private val restTemplate = RestTemplate()

    /**
     * Run Elasticsearch migrations and return success/failure
     */
    fun runMigrations(): Boolean {
        if (!migrationEnabled) {
            logger.info("Elasticsearch migrations are disabled")
            return false
        }

        return try {
            logger.info("Starting Elasticsearch index migrations...")

            if (!checkElasticsearchConnectivity()) {
                logger.warn("Elasticsearch not available, skipping migrations")
                return false
            }

            createFlightRoutesIndex()
            createSampleData()
            logger.info("Elasticsearch migrations completed successfully")
            true
        } catch (e: Exception) {
            logger.error("Elasticsearch migration failed", e)
            false
        }
    }

    /**
     * Check ES connectivity
     */
    private fun checkElasticsearchConnectivity(): Boolean {
        return try {
            val baseUrl = elasticsearchUris.split(",").first()
            restTemplate.getForObject("$baseUrl/_cluster/health", String::class.java)
            logger.info("Elasticsearch connectivity verified")
            true
        } catch (e: Exception) {
            logger.warn("Connectivity check failed: ${e.message}")
            false
        }
    }

    /**
     * Create flight routes index
     */
    private fun createFlightRoutesIndex() {
        try {
            logger.info("Processing flight routes index: $flightRoutesIndex")
            
            val baseUrl = elasticsearchUris.split(",").first()
            
            // Check if index exists
            val exists = try {
                restTemplate.headForHeaders("$baseUrl/$flightRoutesIndex")
                true
            } catch (e: HttpClientErrorException) {
                if (e.statusCode.value() == 404) false else throw e
            }
            
            if (exists && recreateIndices) {
                logger.info("Deleting existing index: $flightRoutesIndex")
                restTemplate.delete("$baseUrl/$flightRoutesIndex")
            }
            
            if (!exists || recreateIndices) {
                logger.info("Creating flight routes index: $flightRoutesIndex")
                
                // Create index with optimized mapping for flight search
                val indexMapping = """
                {
                  "mappings": {
                    "properties": {
                      "routeId": { 
                        "type": "keyword" 
                      },
                      "origin": { 
                        "type": "keyword",
                        "fields": {
                          "text": { "type": "text" }
                        }
                      },
                      "destination": { 
                        "type": "keyword",
                        "fields": {
                          "text": { "type": "text" }
                        }
                      },
                      "airline": { 
                        "type": "keyword",
                        "fields": {
                          "text": { "type": "text" }
                        }
                      },
                      "flightNumber": { 
                        "type": "keyword" 
                      },
                      "departureDateTime": { 
                        "type": "date",
                        "format": "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd HH:mm:ss||epoch_millis"
                      },
                      "arrivalDateTime": { 
                        "type": "date",
                        "format": "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd HH:mm:ss||epoch_millis"
                      },
                      "duration": { 
                        "type": "integer" 
                      },
                      "price": { 
                        "type": "double" 
                      },
                      "availableSeats": { 
                        "type": "integer" 
                      },
                      "aircraft": { 
                        "type": "keyword" 
                      },
                      "stops": { 
                        "type": "integer" 
                      },
                      "routeType": { 
                        "type": "keyword" 
                      },
                      "departureDate": {
                        "type": "date",
                        "format": "yyyy-MM-dd"
                      },
                      "searchKey": {
                        "type": "keyword"
                      },
                      "priceRange": {
                        "type": "keyword"
                      },
                      "durationRange": {
                        "type": "keyword"
                      },
                      "createdAt": { 
                        "type": "date" 
                      },
                      "updatedAt": { 
                        "type": "date" 
                      }
                    }
                  },
                  "settings": {
                    "number_of_shards": 1,
                    "number_of_replicas": 0,
                    "index": {
                      "refresh_interval": "30s",
                      "max_result_window": 50000
                    }
                  }
                }
                """.trimIndent()
                
                val headers = HttpHeaders()
                headers.contentType = MediaType.APPLICATION_JSON
                val entity = HttpEntity(indexMapping, headers)
                
                val response = restTemplate.exchange(
                    "$baseUrl/$flightRoutesIndex",
                    HttpMethod.PUT,
                    entity,
                    String::class.java
                )
                
                logger.info("Successfully created flight routes index: $flightRoutesIndex")
                logger.debug("Index creation response: ${response.body}")
            } else {
                logger.info("Flight routes index already exists: $flightRoutesIndex")
            }
            
        } catch (e: Exception) {
            logger.error("Failed to create flight routes index: $flightRoutesIndex", e)
            throw e
        }
    }

    /**
     * Get Elasticsearch index info
     */
    fun getIndexInfo(): Map<String, Any> {
        return try {
            val baseUrl = elasticsearchUris.split(",").first()
            val indices = mutableMapOf<String, Any>()

            val exists = try {
                restTemplate.headForHeaders("$baseUrl/$flightRoutesIndex")
                true
            } catch (e: HttpClientErrorException) {
                e.statusCode.value() != 404
            }

            if (exists) {
                val stats = try {
                    val result: Map<*, *>? = restTemplate.getForObject("$baseUrl/$flightRoutesIndex/_stats", Map::class.java)
                    result ?: emptyMap<Any, Any>()
                } catch (e: Exception) {
                    mapOf("error" to "Failed to get stats: ${e.message}")
                }

                indices[flightRoutesIndex] = mapOf(
                    "exists" to true,
                    "status" to "ready",
                    "stats" to stats
                )
            } else {
                indices[flightRoutesIndex] = mapOf(
                    "exists" to false,
                    "status" to "missing"
                )
            }
            indices
        } catch (e: Exception) {
            logger.error("Failed to get index info", e)
            mapOf("error" to e.localizedMessage)
        }
    }


    /**
     * Create sample data
     */
    fun createSampleData() {
        if (!migrationEnabled) return
        
        try {
            val baseUrl = elasticsearchUris.split(",").first()
            
            val sampleRoutes = listOf(
                mapOf(
                    "routeId" to "route-del-bom-001",
                    "origin" to "DEL",
                    "destination" to "BOM",
                    "airline" to "AI",
                    "flightNumber" to "AI101",
                    "departureDateTime" to "2024-12-25T10:30:00",
                    "arrivalDateTime" to "2024-12-25T12:45:00",
                    "duration" to 135,
                    "price" to 5500.0,
                    "availableSeats" to 45,
                    "aircraft" to "Boeing 737",
                    "stops" to 0,
                    "routeType" to "DIRECT",
                    "departureDate" to "2024-12-25",
                    "searchKey" to "DEL-BOM-2024-12-25",
                    "priceRange" to "MEDIUM",
                    "durationRange" to "SHORT",
                    "createdAt" to "2024-09-27T17:00:00Z",
                    "updatedAt" to "2024-09-27T17:00:00Z"
                ),
                mapOf(
                    "routeId" to "route-del-bom-002",
                    "origin" to "DEL",
                    "destination" to "BOM",
                    "airline" to "6E",
                    "flightNumber" to "6E202",
                    "departureDateTime" to "2024-12-25T14:15:00",
                    "arrivalDateTime" to "2024-12-25T16:25:00",
                    "duration" to 130,
                    "price" to 4800.0,
                    "availableSeats" to 32,
                    "aircraft" to "Airbus A320",
                    "stops" to 0,
                    "routeType" to "DIRECT",
                    "departureDate" to "2024-12-25",
                    "searchKey" to "DEL-BOM-2024-12-25",
                    "priceRange" to "LOW",
                    "durationRange" to "SHORT",
                    "createdAt" to "2024-09-27T17:00:00Z",
                    "updatedAt" to "2024-09-27T17:00:00Z"
                ),
                mapOf(
                    "routeId" to "route-bom-del-001",
                    "origin" to "BOM",
                    "destination" to "DEL",
                    "airline" to "AI",
                    "flightNumber" to "AI102",
                    "departureDateTime" to "2024-12-25T18:30:00",
                    "arrivalDateTime" to "2024-12-25T20:45:00",
                    "duration" to 135,
                    "price" to 5200.0,
                    "availableSeats" to 28,
                    "aircraft" to "Boeing 737",
                    "stops" to 0,
                    "routeType" to "DIRECT",
                    "departureDate" to "2024-12-25",
                    "searchKey" to "BOM-DEL-2024-12-25",
                    "priceRange" to "MEDIUM",
                    "durationRange" to "SHORT",
                    "createdAt" to "2024-09-27T17:00:00Z",
                    "updatedAt" to "2024-09-27T17:00:00Z"
                )
            )
            
            sampleRoutes.forEachIndexed { index, route ->
                val headers = HttpHeaders()
                headers.contentType = MediaType.APPLICATION_JSON
                val entity = HttpEntity(route, headers)
                
                restTemplate.exchange(
                    "$baseUrl/$flightRoutesIndex/_doc/sample-${index + 1}",
                    HttpMethod.PUT,
                    entity,
                    String::class.java
                )
            }
            
            // Force refresh to make data immediately searchable
            restTemplate.postForObject("$baseUrl/$flightRoutesIndex/_refresh", null, String::class.java)
            
            logger.info("Created ${sampleRoutes.size} sample flight routes")
            
        } catch (e: Exception) {
            logger.warn("Failed to create sample data: ${e.message}")
            throw e
        }
    }
}
