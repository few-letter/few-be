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
            return imageUrls.toList()
        }

        private fun isValidImageUrl(url: String): Boolean =
            try {
                val uri = URI(url)
                NaverConstants.SUPPORT_IMAGE_SUFFIX.any { uri.path.lowercase().endsWith(it) }
            } catch (_: Exception) {
                false
            }
    }
}