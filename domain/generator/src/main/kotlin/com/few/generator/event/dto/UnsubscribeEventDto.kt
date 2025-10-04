package com.few.generator.event.dto

import java.time.LocalDateTime

data class UnsubscribeEventDto(
    val email: String,
    val categories: String,
    val unsubscribedAt: LocalDateTime,
)