package com.airline.search

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SearchEngineApplication

fun main(args: Array<String>) {
    runApplication<SearchEngineApplication>(*args)
}
