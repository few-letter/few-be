package com.few.generator.event.dto

import java.time.LocalDateTime

data class EnrollSubscriptionEventDto(
    val email: String,
    val categories: String,
    val enrolledAt: LocalDateTime,
)