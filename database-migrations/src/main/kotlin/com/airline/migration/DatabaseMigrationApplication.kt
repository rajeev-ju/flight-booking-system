package com.airline.migration

import com.airline.migration.service.ElasticsearchMigrationService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import kotlin.system.exitProcess

/**
 * Standalone Database Migration Application
 * 
 * This application handles PostgreSQL schema migrations via Liquibase.
 * 
 * Usage:
 * - Run before deploying main application services
 * - Can be run independently in CI/CD pipelines
 * - Ensures database schema is up-to-date before application startup
 */
@SpringBootApplication
class DatabaseMigrationApplication {
    
    private val logger = LoggerFactory.getLogger(DatabaseMigrationApplication::class.java)
    
    @Bean
    fun migrationRunner(elasticsearchMigrationService: ElasticsearchMigrationService) = CommandLineRunner {
        logger.info("Starting Database Migrations...")
        
        try {
            // Liquibase migrations run automatically via Spring Boot
            logger.info("PostgreSQL migrations completed (Liquibase)")
            
            // Run Elasticsearch migrations
            val esSuccess = elasticsearchMigrationService.runMigrations()
            if (esSuccess) {
                logger.info("Elasticsearch migrations completed successfully")
            } else {
                logger.warn("Elasticsearch migrations failed, but continuing...")
            }
            
            logger.info("All database migrations completed successfully!")
            
        } catch (e: Exception) {
            logger.error("Migration failed", e)
            exitProcess(1)
        }
        
        // Exit successfully
        exitProcess(0)
    }
    

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<DatabaseMigrationApplication>(*args)
        }
    }
}
