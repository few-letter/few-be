package com.few.generator.controller.response

data class ContentsGeneratorResponse(
    val sourceUrl: String,
    val rawContentId: Long,
    val provisioningContentId: Long,
    val genIds: List<Long>,
)