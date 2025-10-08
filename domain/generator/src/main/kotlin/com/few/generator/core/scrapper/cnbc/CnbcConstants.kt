package com.few.generator.core.scrapper.cnbc

import com.few.common.domain.Category
import com.few.common.domain.ImageSuffix

object CnbcConstants {
    val SUPPORT_IMAGE_SUFFIX = ImageSuffix.entries.map { it.extension.lowercase() }
    val ROOT_URL_MAP: Map<Category, String> =
        mapOf(
            Category.ECONOMY to "https://www.cnbc.com/investing/",
            Category.TECHNOLOGY to "https://www.cnbc.com/technology/",
            Category.POLITICS to "https://www.cnbc.com/politics/",
        )
}