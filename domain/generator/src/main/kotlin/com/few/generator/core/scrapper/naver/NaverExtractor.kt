package com.few.generator.core.scrapper.naver

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URI
import java.util.regex.Pattern

object NaverExtractor {
    object Text {
        fun extract(element: Element): List<String> {
            val textElements = element.select("p, h1, h2, h3, h4, h5, h6, div")
            val allTexts = textElements.flatMap { listOf(it.text(), it.wholeText()) }
            return getTexts(allTexts.joinToString("\n"))
        }

        private fun getTexts(text: String): List<String> {
            val sanitizedText = text.replace("\u00A0", " ").replace("\t", " ")
            val paragraphs = sanitizedText.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
            return paragraphs
                .flatMap { paragraph ->
                    var processedText = paragraph
                    NaverConstants.SENTENCE_SEPARATORS.forEach { sep ->
                        processedText = processedText.replace(sep, "$sep|")
                    }
                    processedText
                        .split("|")
                        .map { it.trim() }
                        .filter { isValidSentence(it) && it.length >= 10 }
                }.distinct()
                .filter { it.split(" ").size >= 4 }
        }

        private fun isValidSentence(sentence: String): Boolean {
            val text = sentence.trim()
            return text.split("\\s+".toRegex()).size >= 4 && text.any { it.isLetterOrDigit() }
        }

        fun extractTitle(document: Document): String =
            document.selectFirst("#title_area")?.text()?.trim()
                ?: throw RuntimeException("Title element with id 'title_area' not found")

        fun extractContent(document: Document): List<String> {
            val contentElement =
                document.selectFirst("#dic_area")
                    ?: throw RuntimeException("Content element with id 'dic_area' not found")

            return contentElement
                .html()
                .split("<br>", "<br/>", "<br />")
                .map { it.replace(Regex("<[^>]*>"), "").trim() }
                .filter { it.isNotEmpty() }
        }
    }

    object Image {
        fun extract(document: Document): List<String> {
            val imageUrls = mutableSetOf<String>()
            document.select("img").forEach { img ->
                img.attr("src").takeIf { isValidImageUrl(it) }?.let { imageUrls.add(it) }
                img.attr("srcset").split(",").map { it.trim().split(" ")[0] }.forEach {
                    if (isValidImageUrl(it)) imageUrls.add(it)
                }
                img.attr("data-src").takeIf { isValidImageUrl(it) }?.let { imageUrls.add(it) }
            }
            document.select("[style]").forEach { tag ->
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
            val htmlStr = document.toString()
            val regexMatches = NaverConstants.IMAGE_URL_REGEX.toPattern().matcher(htmlStr)
            while (regexMatches.find()) {
                regexMatches.group(1)?.takeIf { isValidImageUrl(it) }?.let { imageUrls.add(it) }
            }
            return imageUrls
                .filter { true }
                .filter { it.startsWith("http://") || it.startsWith("https://") }
                .toList()
        }

        fun extractContentImages(document: Document): List<String> {
            val contentElement =
                document.selectFirst("#dic_area")
                    ?: throw RuntimeException("Content element with id 'dic_area' not found")

            val imageUrls = mutableSetOf<String>()
            contentElement.select("img").forEach { img ->
                // 일반적인 src 속성
                img.attr("src").takeIf { it.isNotBlank() && isValidImageUrl(it) }?.let { imageUrls.add(it) }

                // lazy loading을 위한 data-* 속성들
                img.attr("data-src").takeIf { it.isNotBlank() && isValidImageUrl(it) }?.let { imageUrls.add(it) }
                img.attr("data-original").takeIf { it.isNotBlank() && isValidImageUrl(it) }?.let { imageUrls.add(it) }
                img.attr("data-lazy-src").takeIf { it.isNotBlank() && isValidImageUrl(it) }?.let { imageUrls.add(it) }
                img.attr("data-lazy").takeIf { it.isNotBlank() && isValidImageUrl(it) }?.let { imageUrls.add(it) }
                img.attr("data-image").takeIf { it.isNotBlank() && isValidImageUrl(it) }?.let { imageUrls.add(it) }
                img.attr("data-url").takeIf { it.isNotBlank() && isValidImageUrl(it) }?.let { imageUrls.add(it) }

                // srcset 처리 (여러 이미지 URL이 쉼표로 구분됨)
                img.attr("srcset").split(",").forEach { srcsetEntry ->
                    val url = srcsetEntry.trim().split(" ")[0]
                    if (url.isNotBlank() && isValidImageUrl(url)) {
                        imageUrls.add(url)
                    }
                }

                // 모든 속성을 검사하여 이미지 URL 패턴 찾기
                img.attributes().forEach { attr ->
                    val value = attr.value
                    if (value.isNotBlank() &&
                        (value.startsWith("http://") || value.startsWith("https://")) &&
                        isValidImageUrl(value)
                    ) {
                        imageUrls.add(value)
                    }
                }
            }

            return imageUrls
                .filter { true }
                .filter { it.startsWith("http://") || it.startsWith("https://") }
                .toList()
        }

        fun extractThumbnailImageUrl(document: Document): String? =
            document
                .selectFirst("meta[property=og:image]")
                ?.attr("content")
                ?.trim()
                ?.takeIf { it.startsWith("https://") || it.startsWith("http://") }

        private fun isValidImageUrl(url: String): Boolean =
            try {
                val uri = URI(url)
                NaverConstants.SUPPORT_IMAGE_SUFFIX.any { uri.path.lowercase().endsWith(it) }
            } catch (_: Exception) {
                false
            }
    }

    object Url {
        fun extractOrigin(document: Document): String? =
            document
                .select("a")
                .firstOrNull { it.text() == "기사원문" }
                ?.attr("href")
    }
}