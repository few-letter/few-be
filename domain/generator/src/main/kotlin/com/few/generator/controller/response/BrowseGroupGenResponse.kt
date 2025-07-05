package com.few.generator.controller.response

import java.time.LocalDateTime

data class BrowseGroupGenResponses(
    val groups: List<BrowseGroupGenResponse>,
)

data class BrowseGroupGenResponse(
    val id: Long,
    val category: Int,
    val selectedGroupIds: String,
    val headline: String,
    val summary: String,
    val highlightTexts: List<String>,
    val groupSourceHeadlines: List<GroupSourceHeadlineData>,
    val createdAt: LocalDateTime,
)

data class GroupSourceHeadlineData(
    val headline: String,
    val url: String,
)