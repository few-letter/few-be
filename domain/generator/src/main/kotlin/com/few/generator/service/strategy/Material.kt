package com.few.generator.service.strategy

data class Material(
    val provisioningContentsId: Long,
    val title: String? = null,
    val description: String? = null,
    val coreTextsJson: String? = null,
    val headline: String? = null,
    val summary: String? = null,
)