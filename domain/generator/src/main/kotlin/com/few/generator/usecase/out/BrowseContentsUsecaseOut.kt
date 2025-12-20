package com.few.generator.usecase.out

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import java.time.LocalDateTime

data class BrowseContentsUsecaseOut(
    val rawContents: BrowseRawContentsUsecaseOut,
    val provisioningContents: BrowseProvisioningContentsUsecaseOut,
    val gen: BrowseGenUsecaseOut,
)

data class BrowseRawContentsUsecaseOut(
    val id: Long,
    val url: String,
    val title: String,
    val thumbnailImageUrl: String? = null,
    val mediaType: MediaType,
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
    val category: Category,
    val region: Region?,
    val createdAt: LocalDateTime,
)