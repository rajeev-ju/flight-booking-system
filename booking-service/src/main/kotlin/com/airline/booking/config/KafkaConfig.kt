package com.airline.booking.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaConfig {
    
    @Value("\${spring.kafka.bootstrap-servers:localhost:9092}")
    private lateinit var bootstrapServers: String
    
    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val configs = mutableMapOf<String, Any>()
        configs[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configs[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configs[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configs[ProducerConfig.ACKS_CONFIG] = "all"
        configs[ProducerConfig.RETRIES_CONFIG] = 3
        configs[ProducerConfig.BATCH_SIZE_CONFIG] = 16384
        configs[ProducerConfig.LINGER_MS_CONFIG] = 1
        configs[ProducerConfig.BUFFER_MEMORY_CONFIG] = 33554432
        
        return DefaultKafkaProducerFactory(configs)
    }
    
    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }
}
