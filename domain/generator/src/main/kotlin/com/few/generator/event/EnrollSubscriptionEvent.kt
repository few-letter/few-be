package com.few.generator.event

import java.time.LocalDateTime

data class EnrollSubscriptionEvent(
    val email: String,
    val categories: String,
    val enrolledAt: LocalDateTime,
)