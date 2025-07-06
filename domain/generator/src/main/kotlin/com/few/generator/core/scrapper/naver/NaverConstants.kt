package com.few.generator.core.scrapper.naver

import com.few.generator.domain.Category
import com.few.generator.domain.ImageSuffix

object NaverConstants {
    val SUPPORT_IMAGE_SUFFIX = ImageSuffix.entries.map { it.extension.lowercase() }
    val ROOT_URL_MAP: Map<Category, String> =
        mapOf(
            Category.TECHNOLOGY to "https://news.naver.com/section/105",
            Category.LIFE to "https://news.naver.com/section/103",
            Category.POLITICS to "https://news.naver.com/section/100",
            Category.ECONOMY to "https://news.naver.com/section/101",
            Category.SOCIETY to "https://news.naver.com/section/102",
        )
    val NEWS_URL_REGEX = Regex("""https://n\.news\.naver\.com/mnews/article/\d+/\d+$""", RegexOption.IGNORE_CASE)
    val IMAGE_URL_REGEX = Regex("(https?://[^\\s'\"]+\\.(?:jpg|jpeg|png|gif|webp|svg))(?:\\?[^)\\s'\"]+)?", RegexOption.IGNORE_CASE)
    val SENTENCE_SEPARATORS = listOf("... ", ". ", "? ", "! ", "; ", "。", "？", "！")
}