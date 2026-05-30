package com.few.generator.event

import com.few.generator.core.instagram.StockBriefingContent

data class StockBriefingContentProcessedEvent(
    val postId: Long,
    val contents: List<StockBriefingContent>,
    val headlines: List<String> = contents.map { it.headline },
    val mainPageBody: String = "",
)