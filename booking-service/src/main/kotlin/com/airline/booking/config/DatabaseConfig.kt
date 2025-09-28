package com.airline.booking.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableR2dbcRepositories(basePackages = ["com.airline.booking.repository"])
@EnableTransactionManagement
class DatabaseConfig : AbstractR2dbcConfiguration() {
    
    override fun connectionFactory(): ConnectionFactory {
        // This will be overridden by application.yml configuration
        throw UnsupportedOperationException("Use application.yml for database configuration")
    }
    
    @Bean
    fun transactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }
}
