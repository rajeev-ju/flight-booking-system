package com.airline.search.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchConfiguration
import org.springframework.data.elasticsearch.repository.config.EnableReactiveElasticsearchRepositories
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Configuration
@EnableReactiveElasticsearchRepositories(basePackages = ["com.airline.search.repository"])
class ElasticsearchConfig(
    @Value("\${elasticsearch.host:localhost}")
    private val elasticsearchHost: String,
    
    @Value("\${elasticsearch.port:9200}")
    private val elasticsearchPort: Int
) : ReactiveElasticsearchConfiguration() {

    override fun clientConfiguration(): ClientConfiguration {
        return ClientConfiguration.builder()
            .connectedTo("$elasticsearchHost:$elasticsearchPort")
            .withConnectTimeout(Duration.ofSeconds(30))
            .withSocketTimeout(Duration.ofSeconds(60))
            .build()
    }

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB
            }
            .build()
    }
}
