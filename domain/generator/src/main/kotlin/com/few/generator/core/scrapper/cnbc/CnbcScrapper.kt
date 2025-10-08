package com.few.generator.core.scrapper.cnbc

import com.few.common.domain.Category
import com.few.generator.core.scrapper.ScrappedResult
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class CnbcScrapper(
    private val scrapperHttpClient: OkHttpClient,
) {
    private val log = KotlinLogging.logger { }

    fun getRootUrlsByCategory(categories: List<Category>): Map<Category, String> =
        categories
            .filter { it in CnbcConstants.ROOT_URL_MAP }
            .associateWith { CnbcConstants.ROOT_URL_MAP[it]!! }

    fun extractUrlsByCategory(rootUrl: String): Set<String> {
        Thread.sleep((1..5).random() * 1000L)

        log.debug { "[CNBC Category] URLs 추출 시작: $rootUrl" }

        val request = Request.Builder().url(rootUrl).build()
        val html = getHtml(request)

        val yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val datePrefix = "https://www.cnbc.com/$yesterday/"

        val extractedUrls =
            Jsoup
                .parse(html)
                .select("a[href]")
                .mapNotNull { it.attr("href") }
                .filter { it.startsWith(datePrefix) }
                .map { it.split("?")[0] }
                .toSet()

        if (extractedUrls.isEmpty()) {
            throw RuntimeException("[CNBC Category] No matching URLs found in: $rootUrl")
        }

        log.info { "[CNBC Category] URL 추출 완료: $rootUrl -> ${extractedUrls.size}개 URL" }

        return extractedUrls
    }

    fun scrape(url: String): ScrappedResult {
        val request = Request.Builder().url(url).build()
        val html = getHtml(request)

        val document: Document = Jsoup.parse(html)

        val extractTitle = CnbcExtractor.Text.extractTitle(document)
        val extractContents = CnbcExtractor.Text.extractContent(document)
        val extractImgs = CnbcExtractor.Image.extractContentImages(document)
        val extractThumbnailImageUrl = CnbcExtractor.Image.extractThumbnailImageUrl(document)

        return ScrappedResult(
            sourceUrl = url,
            title = extractTitle,
            rawTexts = extractContents,
            images = extractImgs,
            thumbnailImageUrl = extractThumbnailImageUrl,
        )
    }

    private fun getHtml(request: Request): String =
        scrapperHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("[CNBC] HTTP ${response.code} ${response.message} for URL: ${request.url}")
            }
            response.body?.string()
                ?: throw RuntimeException("[CNBC] Empty response body for URL: ${request.url}")
        }
}