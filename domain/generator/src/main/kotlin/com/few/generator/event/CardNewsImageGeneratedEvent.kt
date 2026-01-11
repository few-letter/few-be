package com.few.generator.event

import com.few.common.domain.Region

data class CardNewsImageGeneratedEvent(
    val region: Region,
    val imagePaths: List<String>,
)