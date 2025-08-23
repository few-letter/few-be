package com.few.generator.core.scrapper

import com.few.common.domain.Category
import com.few.generator.core.connection.RetryableJsoup
import com.few.generator.core.scrapper.naver.NaverExtractor
import com.few.generator.core.scrapper.naver.NaverScrapper
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import org.springframework.stereotype.Component

@Component
class Scrapper(
    private val naverScrapper: NaverScrapper,
    private val retryableJsoup: RetryableJsoup,
    private val scrapperHttpClient: OkHttpClient,
) {
    private val log = KotlinLogging.logger {}

    fun extractUrlsByCategories(): Map<Category, Set<String>> =
        naverScrapper
            .getRootUrlsByCategory(Category.entries)
            .mapValues { (_, rootUrl) ->
                naverScrapper.extractUrlsByCategory(rootUrl)
            }

    fun scrape(url: String): ScrappedResult? {
        Thread.sleep((1..5).random().toLong())

        if (url.contains("naver.com")) {
            return naverScrapper.scrape(url)
        } else {
            return retryableJsoup
                .connect(url)
                .let { document ->
                    if (document.title().isBlank()) {
                        log.warn { "Document title is blank for URL: $url" }
                        return null
                    }
                    return naverScrapper.parseDocument(document)
                }
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