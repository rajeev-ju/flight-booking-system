package com.airline.search.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.io.ClassPathResource
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchClient
import org.springframework.stereotype.Component
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest
import co.elastic.clients.elasticsearch.indices.ExistsRequest
import co.elastic.clients.json.JsonData
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream

/**
 * Manages Elasticsearch index creation and migration
 * Ensures indices exist with proper mappings on application startup
 */
@Component
class ElasticsearchIndexManager(
    private val elasticsearchClient: ReactiveElasticsearchClient
) {
    
    private val logger = LoggerFactory.getLogger(ElasticsearchIndexManager::class.java)
    
    @Value("\${app.elasticsearch.index.flight-routes:flight-routes}")
    private lateinit var flightRoutesIndex: String
    
    /**
     * Initialize indices when application is ready
     */
    @EventListener(ApplicationReadyEvent::class)
    fun initializeIndices() {
        runBlocking {
            try {
                logger.info("Initializing Elasticsearch indices...")
                createFlightRoutesIndex()
                logger.info("Elasticsearch indices initialization completed")
            } catch (e: Exception) {
                logger.error("Failed to initialize Elasticsearch indices", e)
                // Don't fail application startup, but log the error
            }
        }
    }
    
    /**
     * Create flight routes index if it doesn't exist
     */
    private suspend fun createFlightRoutesIndex() {
        try {
            // Check if index exists
            val existsRequest = ExistsRequest.Builder()
                .index(flightRoutesIndex)
                .build()
            
            val exists = elasticsearchClient.indices()
                .exists(existsRequest)
                .awaitSingleOrNull()?.value() ?: false
            
            if (!exists) {
                logger.info("Creating flight routes index: $flightRoutesIndex")
                
                // Load mapping from resources
                val mappingResource = ClassPathResource("elasticsearch/flight-routes-mapping.json")
                val mappingJson = mappingResource.inputStream.readBytes().toString(Charsets.UTF_8)
                
                // Create index with mapping
                val createRequest = CreateIndexRequest.Builder()
                    .index(flightRoutesIndex)
                    .withJson(ByteArrayInputStream(mappingJson.toByteArray()))
                    .build()
                
                elasticsearchClient.indices()
                    .create(createRequest)
                    .awaitSingle()
                
                logger.info("Successfully created flight routes index: $flightRoutesIndex")
            } else {
                logger.info("Flight routes index already exists: $flightRoutesIndex")
            }
        } catch (e: Exception) {
            logger.error("Failed to create flight routes index: $flightRoutesIndex", e)
            throw e
        }
    }
    
    /**
     * Manually trigger index creation (for testing or admin purposes)
     */
    suspend fun recreateFlightRoutesIndex() {
        try {
            logger.info("Recreating flight routes index: $flightRoutesIndex")
            
            // Delete index if exists
            val existsRequest = ExistsRequest.Builder()
                .index(flightRoutesIndex)
                .build()
            
            val exists = elasticsearchClient.indices()
                .exists(existsRequest)
                .awaitSingleOrNull()?.value() ?: false
            
            if (exists) {
                elasticsearchClient.indices()
                    .delete { it.index(flightRoutesIndex) }
                    .awaitSingle()
                logger.info("Deleted existing index: $flightRoutesIndex")
            }
            
            // Create new index
            createFlightRoutesIndex()
            
        } catch (e: Exception) {
            logger.error("Failed to recreate flight routes index: $flightRoutesIndex", e)
            throw e
        }
    }
}
