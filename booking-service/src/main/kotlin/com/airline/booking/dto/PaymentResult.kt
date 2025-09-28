package com.airline.booking.dto

data class PaymentResult(
    val success: Boolean,
    val transactionId: String?,
    val message: String
)
