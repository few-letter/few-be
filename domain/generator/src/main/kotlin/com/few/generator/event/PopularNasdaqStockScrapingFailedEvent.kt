package com.few.generator.event

import java.time.LocalDateTime

data class PopularNasdaqStockScrapingFailedEvent(
    val occurredAt: LocalDateTime,
    val errorMessage: String?,
)