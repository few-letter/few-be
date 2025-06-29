package com.few.generator.core.scrapper.naver

import com.few.generator.core.connection.RetryableJsoup
import com.few.generator.core.scrapper.ScrappedResult
import com.few.generator.domain.Category
import com.few.generator.domain.MediaType
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
        val title = removeMediaTypeTitleSuffix(document.selectFirst("title")?.text())
        if (title.isNullOrBlank()) {
            throw RuntimeException("Title is null or blank. URL: ${document.location()}")
        }

        val description = document.selectFirst("meta[name=description]")?.attr("content")?.trim()
        if (description.isNullOrBlank()) {
            throw RuntimeException("Description is null or blank. URL: ${document.location()}")
        }

        val thumbnailImageUrl: String? =
            document
                .selectFirst("meta[property=og:image]")
                ?.attr("content")
                ?.trim()
                ?.takeIf { it.startsWith("https://") || it.startsWith("http://") }
        val rawTexts = NaverExtractor.Text.extract(mainContent)
        if (rawTexts.isEmpty()) {
            throw RuntimeException("No valid raw texts found in the document. URL: ${document.location()}")
        }
        val images = NaverExtractor.Image.extract(document)

        return ScrappedResult(title, description, thumbnailImageUrl, rawTexts, images)
    }

    private fun removeUnnecessaryTags(document: Document) {
        document.select("script, style, nav, footer, header").forEach { it.remove() }
    }

    private fun removeMediaTypeTitleSuffix(title: String?): String? {
        if (title == null) return null

        var trimmedtitle = title.trim()
        for (mediaType in MediaType.entries) {
            if (mediaType == MediaType.ETC) continue
            if (trimmedtitle.endsWith(mediaType.title)) {
                trimmedtitle = trimmedtitle.removeSuffix(mediaType.title).trim()
                return if (trimmedtitle.endsWith("|") || trimmedtitle.endsWith("-")) {
                    trimmedtitle.dropLast(1)
                } else {
                    trimmedtitle
                }
            }
        }

        return trimmedtitle
    }
}