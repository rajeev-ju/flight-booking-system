package com.airline.search.config

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchClient
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ElasticsearchHealthIndicator(
    private val elasticsearchClient: ReactiveElasticsearchClient
) : ReactiveHealthIndicator {
    
    override fun health(): Mono<Health> {
        return elasticsearchClient.info()
            .map { Health.up().build() }
            .onErrorResume { 
                Mono.just(Health.down().withException(it).build())
            }
    }
}
