package com.few.generator.controller.response

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class BrowseContentResponses(
    val contents: List<BrowseContentResponse>,
    val isLast: Boolean,
)

data class BrowseContentResponse(
    val id: Int,
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