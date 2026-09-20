package com.few.generator.event

data class StockBriefingImageGeneratedEvent(
    val postId: Long,
    val detailImagePaths: List<String>,
    val mainPageImagePath: String?,
    val headlines: List<String>,
    val mainPageTitle: String = "",
    val mainPageBody: String = "",
)