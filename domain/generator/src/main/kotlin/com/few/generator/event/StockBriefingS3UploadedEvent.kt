package com.few.generator.event

import java.time.LocalDateTime

data class StockBriefingS3UploadedEvent(
    val postId: Long,
    val uploadTime: LocalDateTime,
    val detailImageUrls: List<String>,
    val mainPageImageUrl: String?,
    val headlines: List<String>,
    val errorMessage: String? = null,
)