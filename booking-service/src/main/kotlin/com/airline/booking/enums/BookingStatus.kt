package com.airline.booking.enums

enum class BookingStatus {
    INITIATED,           // Booking created, starting process
    PAYMENT_PENDING,     // Waiting for payment confirmation
    PAYMENT_CONFIRMED,   // Payment successful
    CONFIRMED,          // Booking fully confirmed
    PAYMENT_FAILED,     // Payment failed
    FAILED,            // General failure
    CANCELLED          // User cancelled the booking
}
