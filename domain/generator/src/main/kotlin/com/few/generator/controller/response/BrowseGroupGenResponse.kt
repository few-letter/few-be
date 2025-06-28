package com.few.generator.controller.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BrowseGroupGenResponse(
    val id: Long,
    val category: Int,
    val groupIndices: String,
    val headline: String,
    val summary: String,
    val highlightTexts: List<String>,
    val groupSourceHeadlines: List<GroupSourceHeadlineData> = emptyList(),
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
)

data class GroupSourceHeadlineData(
    val headline: String,
    val url: String,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BrowseGroupGenResponses(
    val groups: List<BrowseGroupGenResponse>,
)