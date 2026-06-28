package com.few.generator.event

import java.time.LocalDateTime

data class PopularNasdaqCardNewsS3UploadedEvent(
    val uploadTime: LocalDateTime,
    val detailImageUrlsByStock: Map<String, List<String>>,
    val mainPageImageUrlsByStock: Map<String, String>,
    val headlinesByStock: Map<String, List<String>>,
)