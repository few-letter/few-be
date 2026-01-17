package com.few.generator.core.instagram

import java.time.LocalDateTime

data class NewsContent(
    val headline: String,
    val summary: String,
    val category: String,
    val createdAt: LocalDateTime,
    val highlightTexts: List<String> = emptyList(),
)