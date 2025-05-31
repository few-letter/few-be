package com.few.generator.core

import com.few.generator.config.JsoupConnectionFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import web.handler.exception.BadRequestException
import java.net.URI
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
    private val retryCount: Int = 3,
    private val sleepTime: Long = 200,
    private val connectionFactory: JsoupConnectionFactory,
) {
    private val log = KotlinLogging.logger {}
    private val imageExtensions = listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg")

    fun get(url: String): Document? {
        repeat(retryCount) {
            try {
                val response =
                    connectionFactory
                        .createConnection(url)
                        .get()
                Thread.sleep(sleepTime)
                return response
            } catch (e: Exception) {
                log.error { "Request failed: ${e.message}" }
            }
        }
        return null
    }

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
        val soup = get(url) ?: return null

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

        if (title == null || description == null) throw BadRequestException("title 및 description 스크래핑 실패")

        return ScrappedResult(title, description, thumbnailImageUrl, rawTexts, images)
    }

    fun extractUrlsByCategory(rootUrl: String): List<String> {
        repeat(retryCount) { attempt ->
            try {
                return connectionFactory
                    .createConnection(rootUrl)
                    .get()
                    .select("a[href]")
                    .mapNotNull { it.attr("href") }
                    .filter { it.matches(Regex("""https://n\.news\.naver\.com/mnews/article/\d+/\d+$""")) }
                    .map {
                        log.debug { "Extracted URL: $it" }
                        it
                    }.mapNotNull {
                        connectionFactory
                            .createConnection(it)
                            .get()
                            .select("a")
                            .firstOrNull { it.text() == "기사원문" }
                            ?.attr("href")
                    }.map {
                        log.debug { "Origin URL: $it" }
                        it
                    }
            } catch (e: Exception) {
                log.error { "Request failed retrying... : Cause: ${e.message}, attempt: ${attempt + 1}" }
            }

            Thread.sleep(sleepTime)
        }

        return emptyList()
    }
}