package com.few.generator.controller.response

import java.time.LocalDateTime

data class BrowseContentsResponse(
    val rawContents: BrowseRawContentsResponse,
    val provisioningContents: BrowseProvisioningContentsResponse,
    val gens: List<BrowseGenResponse>,
)

data class BrowseRawContentsResponse(
    val id: Long,
    val url: String,
    val title: String,
    val description: String,
    val thumbnailImageUrl: String? = null,
    val rawTexts: String,
    val imageUrls: String = "[]",
    val createdAt: LocalDateTime,
)

data class BrowseProvisioningContentsResponse(
    val id: Long,
    val rawContentsId: Long,
    val completionIds: List<String>,
    val bodyTextsJson: String = "[]",
    val coreTextsJson: String = "[]",
    val createdAt: LocalDateTime,
)

data class BrowseGenResponse(
    val id: Long,
    val provisioningContentsId: Long,
    val completionIds: List<String>,
    val headline: String,
    val summary: String,
    val highlightTexts: String = "[]",
    val createdAt: LocalDateTime,
)