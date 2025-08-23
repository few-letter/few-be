package com.few.generator.core.scrapper.naver

import com.few.common.domain.Category
import com.few.common.domain.MediaType
import com.few.generator.core.scrapper.ScrappedResult
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Service

@Service
class NaverScrapper(
    private val scrapperHttpClient: OkHttpClient,
) {
    private val log = KotlinLogging.logger { }

    fun getRootUrlsByCategory(categories: List<Category>): Map<Category, String> =
        categories
            .filter { it in NaverConstants.ROOT_URL_MAP }
            .associateWith { NaverConstants.ROOT_URL_MAP[it]!! }

    fun extractUrlsByCategory(rootUrl: String): Set<String> {
        Thread.sleep((1..5).random() * 1000L)

        log.debug { "[NAVER Category] URLs 추출 시작: $rootUrl" }

        val request = Request.Builder().url(rootUrl).build()
        val html = getHtml(request)

        val extractedUrls =
            Jsoup
                .parse(html)
                .select("a[href]")
                .mapNotNull { it.attr("href") }
                .filter { it.matches(NaverConstants.NEWS_URL_REGEX) }
                .map { it.replace("amp;", "") }
                .toSet()

        if (extractedUrls.isEmpty()) {
            throw RuntimeException("[NAVER Category] No matching URLs found in: $rootUrl")
        }

        log.info { "[NAVER Category] URL 추출 완료: $rootUrl -> ${extractedUrls.size}개 URL" }

        return extractedUrls
    }

    fun scrape(url: String): ScrappedResult {
        val request = Request.Builder().url(url).build()
        val html = getHtml(request)

        val document: Document =
            Jsoup
                .parse(html)

        removeUnnecessaryTags(document)

        val sourceUrl = NaverExtractor.Url.extractOrigin(document) ?: url
        val extractTitle = removeMediaTypeTitleSuffix(NaverExtractor.Text.extractTitle(document))
        val extractContents = NaverExtractor.Text.extractContent(document)
        val extractImgs = NaverExtractor.Image.extractContentImages(document)
        val extractThumbnailImageUrl = NaverExtractor.Image.extractThumbnailImageUrl(document)

        return ScrappedResult(
            sourceUrl = sourceUrl,
            title = extractTitle,
            rawTexts = extractContents,
            images = extractImgs,
            thumbnailImageUrl = extractThumbnailImageUrl,
        )
    }

    private fun getHtml(request: Request): String =
        scrapperHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("[NAVER] HTTP ${response.code} ${response.message} for URL: ${request.url}")
            }
            response.body?.string()
                ?: throw RuntimeException("[NAVER] Empty response body for URL: ${request.url}")
        }

    private fun removeUnnecessaryTags(document: Document) {
        document.select("script, style, nav, footer, header").forEach { it.remove() }
    }

    private fun removeMediaTypeTitleSuffix(title: String?): String {
        if (title == null) {
            throw RuntimeException("Title is null")
        }

        var trimmedtitle = title.trim()

        if (trimmedtitle.startsWith("[속보]")) {
            trimmedtitle = trimmedtitle.removePrefix("[속보]")
        }
        if (trimmedtitle.startsWith("[AI픽]")) {
            trimmedtitle = trimmedtitle.removePrefix("[AI픽]")
        }
        if (trimmedtitle.endsWith("(종합)")) {
            trimmedtitle = trimmedtitle.removeSuffix("(종합)")
        }
        if (trimmedtitle.startsWith("[뉴스in뉴스]")) {
            trimmedtitle = trimmedtitle.removeSuffix("[뉴스in뉴스]")
        }

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