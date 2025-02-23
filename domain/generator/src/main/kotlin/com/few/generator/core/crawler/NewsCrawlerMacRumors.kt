package com.few.generator.core.crawler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NewsCrawlerMacRumors(
    private val generatorRestTemplate: RestTemplate,
) : BaseNewsCrawler(generatorRestTemplate) {
    companion object {
        private const val PRESS_NAME = "MacRumors"
        private const val RSS_URL = "https://feeds.macrumors.com/MacRumors-All"
    }

    override fun getAvailableTopics(): List<String> = listOf("apple")

    override fun getNewsUrls(
        topic: String,
        limit: Int,
    ): List<String> {
        logger.info { "MacRumors 뉴스 링크를 수집합니다." }
        val urls = mutableListOf<String>()

        val soup: Document? = getSoup(RSS_URL)
        if (soup == null) {
            logger.error { "Failed to fetch RSS feed from $RSS_URL" }
            return emptyList()
        }

        val items = soup.select("item")
        for (item in items) {
            var url: String? = null

            // 1. link 태그에서 URL 찾기
            val link = item.selectFirst("link")?.text()?.trim()
            if (!link.isNullOrBlank()) {
                urls.add(link)
                if (urls.size >= limit) return urls
                continue
            }

            // 2. guid 태그에서 URL 찾기 (백업)
            val guid = item.selectFirst("guid")?.text()?.trim()
            if (!guid.isNullOrBlank() && guid.startsWith("http")) {
                urls.add(guid)
                if (urls.size >= limit) return urls
            }
        }

        logger.info { "Found ${urls.size} URLs from MacRumors RSS feed" }
        return urls.take(limit)
    }

    override fun getSoup(url: String): Document? {
        val xml = getRequest(url)
        return try {
            xml?.let {
                Jsoup.parse(
                    it,
                    "",
                    org.jsoup.parser.Parser
                        .xmlParser(),
                )
            }
        } catch (e: Exception) {
            logger.error { "Failed to parse XML: ${e.message}" }
            null
        }
    }
}