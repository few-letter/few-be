package com.few.generator.event

import java.time.LocalDateTime

data class UnsubscribeEvent(
    val email: String,
    val categories: String,
    val unsubscribedAt: LocalDateTime,
)