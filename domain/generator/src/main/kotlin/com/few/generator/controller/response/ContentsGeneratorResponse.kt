package com.few.generator.controller.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ContentsGeneratorResponse(
    val sourceUrl: String,
    val rawContentId: Long,
    val provisioningContentId: Long,
    val genIds: List<Long>? = null,
)