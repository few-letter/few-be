package com.few.generator.event

import com.few.common.domain.Category
import com.few.common.domain.Region

data class CardNewsImageGeneratedEvent(
    val region: Region,
    val imagePathsByCategory: Map<Category, List<String>>,
    val mainPageImagePathsByCategory: Map<Category, String> = emptyMap(),
)