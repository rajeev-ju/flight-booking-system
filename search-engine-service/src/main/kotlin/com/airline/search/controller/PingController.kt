package com.airline.search.controller

import com.airline.shared.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PingController {
    @GetMapping("/ping")
    fun ping(): ResponseEntity<ApiResponse<String>> {
        return ResponseEntity.ok(ApiResponse(success = true, data = HttpStatus.OK.value().toString()))
    }
}
