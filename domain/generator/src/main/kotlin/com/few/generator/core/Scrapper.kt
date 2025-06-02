package com.few.generator.core

import com.few.generator.config.JsoupConnectionFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.coyote.BadRequestException
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class ScrappedResult(
    val title: String = "",
    val description: String = "",
    val thumbnailImageUrl: String = "",
    val rawTexts: List<String> = emptyList(),
    val images: List<String> = emptyList(),
)

@Component
class Scrapper(
    private val connectionFactory: JsoupConnectionFactory,
    @Value("\${generator.scraping.maxRetries}")
    private val maxRetries: Int,
    @Value("\${generator.scraping.defaultRetryAfter}")
    private val defaultRetryAfter: Long,
) {
    private val log = KotlinLogging.logger {}
    private val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg")

    fun isValidSentence(sentence: String): Boolean {
        val text = sentence.trim()
        return text.split("\\s+".toRegex()).size >= 4 && text.any { it.isLetterOrDigit() }
    }

    fun getTexts(text: String): List<String> {
        val texts = mutableListOf<String>()
        val sanitizedText = text.replace("\u00A0", " ").replace("\t", " ")
        val paragraphs = sanitizedText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

        paragraphs.forEach { paragraph ->
            val separators = listOf("... ", ". ", "? ", "! ", "; ", "。", "？", "！")
            var processedText = paragraph
            separators.forEach { sep -> processedText = processedText.replace(sep, "$sep|") }
            val sentences = processedText.split("|").map { it.trim() }
            texts.addAll(sentences.filter { isValidSentence(it) && it.length >= 10 })
        }

        return texts.distinct().filter { it.split(" ").size >= 4 }
    }

    fun isValidImageUrl(url: String): Boolean =
        try {
            val uri = URI(url)
            imageExtensions.any { uri.path.lowercase().endsWith(it) }
        } catch (e: Exception) {
            false
        }

    fun getImages(soup: Document): List<String> {
        val imageUrls = mutableSetOf<String>()

        soup.select("img").forEach { img ->
            img.attr("src")?.takeIf { isValidImageUrl(it) }?.let { imageUrls.add(it) }
            img.attr("srcset")?.split(",")?.map { it.trim().split(" ")[0] }?.forEach {
                if (isValidImageUrl(it)) imageUrls.add(it)
            }
            img.attr("data-src")?.takeIf { isValidImageUrl(it) }?.let { imageUrls.add(it) }
        }

        soup.select("[style]").forEach { tag ->
            val style = tag.attr("style")
            val foundUrls = Pattern.compile("url\\(([^)]+)\\)").matcher(style)
            while (foundUrls.find()) {
                val cleanedUrl =
                    foundUrls
                        .group(1)
                        .replace("\"", "")
                        .replace("'", "")
                        .trim()
                if (isValidImageUrl(cleanedUrl)) imageUrls.add(cleanedUrl)
            }
        }

        val htmlStr = soup.toString()
        val regexPattern = "(https?://[^\\s'\"]+\\.(?:jpg|jpeg|png|gif|webp|svg))(?:\\?[^)\\s'\"]+)?"
        val regexMatches = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE).matcher(htmlStr)
        while (regexMatches.find()) {
            regexMatches.group(1)?.takeIf { isValidImageUrl(it) }?.let { imageUrls.add(it) }
        }

        return imageUrls.toList()
    }

    fun scrape(url: String): ScrappedResult? {
        val soup = getWithRetry(url)

        soup.select("script, style, nav, footer, header").forEach { it.remove() }

        val mainContent = soup.selectFirst("main, article, div.content") ?: soup
        val title = soup.selectFirst("title")?.text()?.trim()
        val description = soup.selectFirst("meta[name=description]")?.attr("content")?.trim()
        val thumbnailImageUrl = soup.selectFirst("meta[property=og:image]")?.attr("content")?.trim() ?: ""

        val textElements = mainContent.select("p, h1, h2, h3, h4, h5, h6, div")
        val allTexts = mutableListOf<String>()
        textElements.forEach { element ->
            element.text()?.let { allTexts.add(it) }
            element.wholeText()?.let { allTexts.add(it) }
        }

        val rawTexts = getTexts(allTexts.joinToString("\n"))
        val images = getImages(soup)

        if (title == null || description == null) {
            log.error { "title 및 description 스크래핑 실패. URL: $url" }
            return null
        }

        return ScrappedResult(title, description, thumbnailImageUrl, rawTexts, images)
    }

    fun extractUrlsByCategory(rootUrl: String): Set<String> =
        getWithRetry(rootUrl)
            .select("a[href]")
            .mapNotNull { it.attr("href") }
            .filter { it.matches(Regex("""https://n\.news\.naver\.com/mnews/article/\d+/\d+$""")) }
            .map {
                log.debug { "Extracted URL: $it" }
                it
            }.mapNotNull {
                getWithRetry(it)
                    .select("a")
                    .firstOrNull { it.text() == "기사원문" }
                    ?.attr("href")
            }.toSet()

    private fun getWithRetry(url: String): Document {
        var attempt = 0

        while (attempt < maxRetries) {
            val response =
                connectionFactory
                    .createConnection(url)
                    .execute()

            if (response.statusCode() == 429) {
                val retryAfter =
                    Math.min(
                        (response.header("Retry-After")?.toLongOrNull() ?: defaultRetryAfter),
                        defaultRetryAfter,
                    )
                TimeUnit.SECONDS.sleep(retryAfter + 1)
                attempt++
                continue
            }

            return response.parse()
        }

        throw BadRequestException("Failed to fetch document after $maxRetries attempts for URL: $url")
    }
}