package com.few.generator.usecase.out

import common.domain.Category
import common.domain.MediaType
import java.time.LocalDateTime

data class BrowseContentsUsecaseOuts(
    val contents: List<ContentsUsecaseOut>,
    val isLast: Boolean,
)

data class ContentsUsecaseOut(
    val id: Long,
    val url: String,
    val thumbnailImageUrl: String?,
    val mediaType: MediaType,
    val headline: String,
    val summary: String,
    val highlightTexts: List<String>,
    val createdAt: LocalDateTime,
    val category: Category,
)