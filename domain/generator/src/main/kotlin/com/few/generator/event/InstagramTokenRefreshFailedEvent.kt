package com.few.generator.event

import java.time.LocalDateTime

data class InstagramTokenRefreshFailedEvent(
    val occurredAt: LocalDateTime,
    val errorMessage: String?,
)