package com.few.generator.event

import com.few.common.domain.Category
import com.few.common.domain.Region
import java.time.LocalDateTime

data class InstagramUploadCompletedEvent(
    val region: Region,
    val uploadTime: LocalDateTime,
    val successCategories: List<Category>,
    val failedCategories: List<Category>,
    val errorMessages: Map<Category, String> = emptyMap(),
)