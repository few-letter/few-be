package com.few.generator.core.scrapper

import com.few.generator.core.connection.RetryAbleJsoup
import com.few.generator.core.scrapper.naver.NaverScrapper
import com.few.generator.domain.Category
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

data class ScrappedResult(
    val title: String = "",
    val description: String = "",
    val thumbnailImageUrl: String = "",
    val rawTexts: List<String> = emptyList(),
    val images: List<String> = emptyList(),
)

@Component
class Scrapper(
    private val scrappingSupporter: NaverScrapper,
    private val retryAbleJsoup: RetryAbleJsoup,
) {
    private val log = KotlinLogging.logger {}

    fun extractUrlsByCategories(): Map<Category, Set<String>> =
        scrappingSupporter
            .getRootUrlsByCategory(Category.entries)
            .mapValues { (_, rootUrl) ->
                scrappingSupporter.extractUrlsByCategory(rootUrl)
            }

    fun scrape(url: String): ScrappedResult? {
        retryAbleJsoup
            .connect(url, { (1..5).random().toLong() })
            .let { document ->
                if (document.title().isBlank()) {
                    log.warn { "Document title is blank for URL: $url" }
                    return null
                }
                return scrappingSupporter.parseDocument(document)
            }
    }

    fun extractOriginUrl(url: String): String? =
        retryAbleJsoup
            .connect(url) { (1..5).random().toLong() }
            .select("a")
            .firstOrNull { it.text() == "기사원문" }
            ?.attr("href")
}