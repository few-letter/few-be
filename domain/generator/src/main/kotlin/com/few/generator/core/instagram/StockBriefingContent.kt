package com.few.generator.core.instagram

data class StockBriefingContent(
    val headline: String,
    val summary: String,
    val highlightTexts: List<String> = emptyList(),
)