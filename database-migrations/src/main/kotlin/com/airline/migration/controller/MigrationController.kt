package com.airline.migration.controller

import com.airline.migration.service.ElasticsearchMigrationService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/migration")
class MigrationController(
    private val elasticsearchMigrationService: ElasticsearchMigrationService
) {

    private val logger = LoggerFactory.getLogger(MigrationController::class.java)

    /**
     * Get overall migration status
     */
    @GetMapping("/status")
    fun getMigrationStatus(): Map<String, Any> {
        logger.info("Fetching migration status")
        return mapOf(
            "postgresql" to mapOf(
                "status" to "completed",
                "message" to "Liquibase migrations handled automatically"
            ),
            "elasticsearch" to elasticsearchMigrationService.getIndexInfo()
        )
    }

    /**
     * Get Elasticsearch index information
     */
    @GetMapping("/elasticsearch/indices")
    fun getElasticsearchIndices(): Map<String, Any> {
        logger.info("Fetching Elasticsearch indices information")
        return elasticsearchMigrationService.getIndexInfo()
    }

    /**
     * Manually trigger Elasticsearch migrations
     */
    @PostMapping("/elasticsearch/migrate")
    fun triggerElasticsearchMigration(): ResponseEntity<Map<String, Any>> {
        logger.info("Manually triggering Elasticsearch migration")
        return try {
            val success = elasticsearchMigrationService.runMigrations()
            if (success) {
                ResponseEntity.ok(
                    mapOf(
                        "success" to true,
                        "message" to "Elasticsearch migrations completed successfully"
                    )
                )
            } else {
                ResponseEntity.status(500).body(
                    mapOf(
                        "success" to false,
                        "message" to "Elasticsearch migration failed"
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Elasticsearch migration failed", e)
            ResponseEntity.status(500).body(
                mapOf(
                    "success" to false,
                    "message" to "Elasticsearch migration failed: ${e.message}"
                )
            )
        }
    }

    /**
     * Create sample data for testing
     */
    @PostMapping("/elasticsearch/sample-data")
    fun createSampleData(): ResponseEntity<Map<String, Any>> {
        logger.info("Creating sample data in Elasticsearch")
        return try {
            elasticsearchMigrationService.createSampleData()
            ResponseEntity.ok(
                mapOf(
                    "success" to true,
                    "message" to "Sample data created successfully"
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to create sample data", e)
            ResponseEntity.status(500).body(
                mapOf(
                    "success" to false,
                    "message" to "Failed to create sample data: ${e.message}"
                )
            )
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    fun health(): Map<String, Any> {
        logger.info("Health check called")
        return mapOf(
            "status" to "UP",
            "service" to "database-migrations",
            "elasticsearch" to elasticsearchMigrationService.getIndexInfo()
        )
    }
}
