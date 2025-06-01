package com.few.generator.usecase.out

data class ContentsGeneratorUseCaseOut(
    val sourceUrl: String,
    val rawContentId: Long,
    val provisioningContentId: Long,
    val genIds: List<Long>? = null,
)