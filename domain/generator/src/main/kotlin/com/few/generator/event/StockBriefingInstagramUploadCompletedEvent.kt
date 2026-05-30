package com.few.generator.event

import java.time.LocalDateTime

data class StockBriefingInstagramUploadCompletedEvent(
    val postId: Long,
    val uploadTime: LocalDateTime,
    val success: Boolean,
    val failedStage: String? = null,
    val errorMessage: String? = null,
)