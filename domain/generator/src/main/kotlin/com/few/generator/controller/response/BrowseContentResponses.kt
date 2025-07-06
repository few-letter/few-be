package com.few.generator.controller.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class BrowseContentResponses(
    val contents: List<BrowseContentResponse>,
    @get:JsonProperty("isLast")
    val isLast: Boolean,
)

data class BrowseContentResponse(
    val id: Long,
    val url: String,
    val thumbnailImageUrl: String?,
    val mediaType: CodeValueResponse,
    val headline: String,
    val summary: String,
    val highlightTexts: List<String>,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
    val category: CodeValueResponse,
)