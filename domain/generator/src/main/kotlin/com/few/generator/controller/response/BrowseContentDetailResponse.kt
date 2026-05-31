package com.few.generator.controller.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BrowseContentDetailResponse(
    val id: Long,
    val thumbnailImageUrl: String?,
    val mediaType: CodeValueResponse,
    val url: String,
    val headline: String,
    val summary: String,
    val highlightTexts: List<String>,
    val category: CodeValueResponse,
    val region: CodeValueResponse?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
)