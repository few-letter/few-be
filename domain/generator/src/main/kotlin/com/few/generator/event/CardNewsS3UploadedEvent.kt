package com.few.generator.event

import com.few.common.domain.Category
import com.few.common.domain.Region
import java.time.LocalDateTime

data class CardNewsS3UploadedEvent(
    val region: Region,
    val uploadedCount: Int,
    val totalCount: Int,
    val uploadTime: LocalDateTime,
    val uploadedUrlsByCategory: Map<Category, List<String>> = emptyMap(),
    val mainPageUrlsByCategory: Map<Category, String> = emptyMap(),
    val errorMessage: String? = null,
)