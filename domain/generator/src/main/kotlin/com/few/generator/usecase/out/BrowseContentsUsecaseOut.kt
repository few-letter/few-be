package com.few.generator.usecase.out

import java.time.LocalDateTime

data class BrowseContentsUsecaseOut(
    val rawContents: BrowseRawContentsUsecaseOut,
    val provisioningContents: BrowseProvisioningContentsUsecaseOut,
    val gens: List<BrowseGenUsecaseOut>,
)

data class BrowseRawContentsUsecaseOut(
    val id: Long,
    val url: String,
    val title: String,
    val description: String,
    val thumbnailImageUrl: String? = null,
    val rawTexts: String,
    val imageUrls: List<String>,
    val createdAt: LocalDateTime,
)

data class BrowseProvisioningContentsUsecaseOut(
    val id: Long,
    val rawContentsId: Long,
    val completionIds: List<String>,
    val bodyTextsJson: List<String>,
    val coreTextsJson: List<String>,
    val createdAt: LocalDateTime,
)

data class BrowseGenUsecaseOut(
    val id: Long,
    val provisioningContentsId: Long,
    val completionIds: List<String>,
    val headline: String,
    val summary: String,
    val highlightTexts: List<String>,
    val type: String,
    val createdAt: LocalDateTime,
)