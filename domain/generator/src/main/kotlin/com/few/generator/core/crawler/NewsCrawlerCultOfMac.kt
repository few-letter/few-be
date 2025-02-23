package com.few.generator.core.crawler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NewsCrawlerCultOfMac(
    private val generatorRestTemplate: RestTemplate,
) : BaseNewsCrawler(generatorRestTemplate) {
    companion object {
        private const val PRESS_NAME = "Cult of Mac"
        private const val RSS_URL = "https://www.cultofmac.com/feed"
    }

    override fun getAvailableTopics(): List<String> {
        return listOf("apple") // 애플 관련 뉴스만 다룸
    }

    override fun getNewsUrls(
        topic: String,
        limit: Int,
    ): List<String> {
        logger.info { "Cult of Mac 뉴스 링크를 수집합니다." }
        val urls = mutableListOf<String>()

        val soup: Document? = getSoup(RSS_URL)
        if (soup == null) {
            logger.error { "Failed to fetch RSS feed from $RSS_URL" }
            return emptyList()
        }

        val items = soup.select("item")
        for (item in items) {
            val link = item.selectFirst("link")?.text()?.trim()
            if (!link.isNullOrBlank()) {
                urls.add(link)
                if (urls.size >= limit) break
            }
        }

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