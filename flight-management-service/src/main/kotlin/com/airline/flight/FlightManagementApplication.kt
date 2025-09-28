package com.airline.flight

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class FlightManagementApplication

fun main(args: Array<String>) {
    runApplication<FlightManagementApplication>(*args)
}
