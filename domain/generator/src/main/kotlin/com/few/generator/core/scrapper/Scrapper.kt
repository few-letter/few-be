package com.few.generator.core.scrapper

import com.few.common.domain.Category
import com.few.generator.core.connection.RetryableJsoup
import com.few.generator.core.scrapper.naver.NaverExtractor
import com.few.generator.core.scrapper.naver.NaverScrapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

data class ScrappedResult(
    val title: String = "",
    val description: String = "",
    val thumbnailImageUrl: String? = null,
    val rawTexts: List<String> = emptyList(),
    val images: List<String> = emptyList(),
)

@Component
class Scrapper(
    private val naverScrapper: NaverScrapper,
    private val retryableJsoup: RetryableJsoup,
) {
    private val log = KotlinLogging.logger {}

    fun extractUrlsByCategories(): Map<Category, Set<String>> =
        naverScrapper
            .getRootUrlsByCategory(Category.entries)
            .mapValues { (_, rootUrl) ->
                naverScrapper.extractUrlsByCategory(rootUrl)
            }

    fun scrape(url: String): ScrappedResult? {
        // Introduce a random sleep time to avoid hitting the server too quickly
        Thread.sleep((1..5).random().toLong())
        retryableJsoup
            .connect(url)
            .let { document ->
                if (document.title().isBlank()) {
                    log.warn { "Document title is blank for URL: $url" }
                    return null
                }
                return naverScrapper.parseDocument(document)
            }
    }

    fun extractOriginUrl(url: String): String? {
        // Introduce a random sleep time to avoid hitting the server too quickly
        Thread.sleep((1..5).random().toLong())
        return retryableJsoup
            .connect(url)
            .let {
                NaverExtractor.Url.extractOrigin(it)
            }
    }
}