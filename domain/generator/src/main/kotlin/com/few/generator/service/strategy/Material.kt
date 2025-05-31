package com.few.generator.service.strategy

import com.few.generator.domain.Category

data class Material(
    val provisioningContentsId: Long,
    val title: String? = null,
    val description: String? = null,
    val coreTextsJson: String? = null,
    val headline: String? = null,
    val summary: String? = null,
    val category: Category,
)