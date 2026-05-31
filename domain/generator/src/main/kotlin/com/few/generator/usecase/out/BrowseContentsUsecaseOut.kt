package com.few.generator.usecase.out

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.common.domain.Region
import java.time.LocalDateTime

data class BrowseContentsUsecaseOut(
    val id: Long,
    val url: String,
    val thumbnailImageUrl: String?,
    val mediaType: MediaType,
    val headline: String,
    val summary: String,
    val highlightTexts: List<String>,
    val category: Category,
    val region: Region?,
    val createdAt: LocalDateTime,
)