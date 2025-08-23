package com.few.generator.core.scrapper

data class ScrappedResult(
    val sourceUrl: String = "",
    val title: String = "",
    val description: String = "",
    val thumbnailImageUrl: String? = null,
    val rawTexts: List<String> = emptyList(),
    val images: List<String> = emptyList(),
)