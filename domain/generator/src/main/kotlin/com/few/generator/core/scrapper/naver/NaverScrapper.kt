package com.few.generator.core.scrapper.naver

import com.few.generator.core.connection.RetryableJsoup
import com.few.generator.core.scrapper.ScrappedResult
import com.few.generator.domain.Category
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.nodes.Document
import org.springframework.stereotype.Service

@Service
class NaverScrapper(
    private val retryableJsoup: RetryableJsoup,
) {
    private val log = KotlinLogging.logger { }

    fun getRootUrlsByCategory(categories: List<Category>): Map<Category, String> =
        categories
            .filter { it in NaverConstants.ROOT_URL_MAP }
            .associateWith { NaverConstants.ROOT_URL_MAP[it]!! }

    fun extractUrlsByCategory(rootUrl: String): Set<String> {
        // Introduce a random sleep time to avoid hitting the server too quickly
        Thread.sleep((1..5).random().toLong())
        return retryableJsoup
            .connect(rootUrl)
            .select("a[href]")
            .mapNotNull { it.attr("href") }
            .filter { it.matches(NaverConstants.NEWS_URL_REGEX) }
            .map { it.replace("amp;", "") }
            .toSet()
    }

    fun parseDocument(document: Document): ScrappedResult? {
        removeUnnecessaryTags(document)
        val mainContent = document.selectFirst("main, article, div.content") ?: document
        val title = document.selectFirst("title")?.text()?.trim()
        val description = document.selectFirst("meta[name=description]")?.attr("content")?.trim()
        val thumbnailImageUrl: String? =
            document
                .selectFirst("meta[property=og:image]")
                ?.attr("content")
                ?.trim()
                ?.takeIf { it.startsWith("https://") || it.startsWith("http://") }
        val rawTexts = NaverExtractor.Text.extract(mainContent)
        val images = NaverExtractor.Image.extract(document)

        if (title.isNullOrBlank() || description.isNullOrBlank()) {
            log.error { "title 및 description 스크래핑 실패. URL: ${document.location()}" }
            return null
        }
        return ScrappedResult(title, description, thumbnailImageUrl, rawTexts, images)
    }

    private fun removeUnnecessaryTags(document: Document) {
        document.select("script, style, nav, footer, header").forEach { it.remove() }
    }
}