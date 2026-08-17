package com.few.generator.event

import java.time.LocalDate

data class GenCacheMetricsCollectFailedEvent(
    val date: LocalDate,
    val errorMessage: String,
)