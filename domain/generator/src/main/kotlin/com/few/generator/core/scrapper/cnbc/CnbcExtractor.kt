package com.few.generator.core.scrapper.cnbc

import org.jsoup.nodes.Document
import java.net.URI

object CnbcExtractor {
    object Text {
        fun extractTitle(document: Document): String =
            document.selectFirst("h1.ArticleHeader-headline")?.text()?.trim()
                ?: document.selectFirst("h1")?.text()?.trim()
                ?: throw RuntimeException("Title element not found")

        fun extractContent(document: Document): List<String> {
            val contentElement =
                document.selectFirst("div.ArticleBody-articleBody")
                    ?: document.selectFirst("div.group")
                    ?: throw RuntimeException("Content element not found")

            val blocks =
                contentElement
                    .select("p")
                    .eachText()
                    .map { it.replace("\u00A0", " ").trim() }
                    .filter { it.isNotEmpty() }
                    .filter { it.length >= 10 }

            return blocks.distinct()
        }
    }

    object Image {
        fun extractContentImages(document: Document): List<String> {
            val imageUrls = mutableSetOf<String>()

            document.select("img").forEach { img ->
                img.attr("src").takeIf { it.isNotBlank() && isValidImageUrl(it) }?.let { imageUrls.add(it) }
                img.attr("data-src").takeIf { it.isNotBlank() && isValidImageUrl(it) }?.let { imageUrls.add(it) }
                img.attr("data-original").takeIf { it.isNotBlank() && isValidImageUrl(it) }?.let { imageUrls.add(it) }

                img.attr("srcset").split(",").forEach { srcsetEntry ->
                    val url = srcsetEntry.trim().split(" ")[0]
                    if (url.isNotBlank() && isValidImageUrl(url)) {
                        imageUrls.add(url)
                    }
                }
            }

            return imageUrls
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
                CnbcConstants.SUPPORT_IMAGE_SUFFIX.any { uri.path.lowercase().endsWith(it) }
            } catch (_: Exception) {
                false
            }
    }
}