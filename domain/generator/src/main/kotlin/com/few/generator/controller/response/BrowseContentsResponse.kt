package com.few.generator.controller.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BrowseContentsResponse(
    val rawContents: BrowseRawContentsResponse,
    val provisioningContents: BrowseProvisioningContentsResponse,
    val gen: BrowseGenResponse,
)

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

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BrowseRawContentsResponse(
    val id: Long,
    val url: String,
    val title: String,
    val thumbnailImageUrl: String? = null,
    val mediaType: CodeValueResponse,
    val rawTexts: String,
    val imageUrls: List<String>,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BrowseProvisioningContentsResponse(
    val id: Long,
    val rawContentsId: Long,
    val completionIds: List<String>,
    val bodyTextsJson: List<String>,
    val coreTextsJson: List<String>,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BrowseGenResponse(
    val id: Long,
    val provisioningContentsId: Long,
    val completionIds: List<String>,
    val headline: String,
    val summary: String,
    val highlightTexts: List<String>,
    val category: CodeValueResponse,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
)