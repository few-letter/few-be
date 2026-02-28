package com.few.generator.domain

data class RawContents(
    val url: String,
    val title: String,
    val thumbnailImageUrl: String? = null,
    val rawTexts: String,
    val imageUrls: String = "[]",
    val category: Int,
    val mediaType: Int,
    val region: Int,
)