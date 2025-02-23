package com.few.generator.core.crawler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NewsCrawlerElectrek(
    private val generatorRestTemplate: RestTemplate,
) : BaseNewsCrawler(generatorRestTemplate) {
    companion object {
        private const val PRESS_NAME = "Electrek"
        private const val RSS_URL = "https://electrek.co/feed/"
    }

    override fun getAvailableTopics(): List<String> {
        return listOf("ev") // 전기차 관련 뉴스만 다룸
    }

    override fun getNewsUrls(
        topic: String,
        limit: Int,
    ): List<String> {
        logger.info { "Electrek 뉴스 링크를 수집합니다." }
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
            val link = item.selectFirst("link")
            if (link != null) {
                url = link.text()?.trim()
                if (url.isNullOrBlank()) {
                    val nextSibling = link.nextSibling()?.toString()?.trim()
                    if (!nextSibling.isNullOrBlank() && nextSibling.startsWith("http")) {
                        url = nextSibling
                    }
                }
            }

            // 2. guid 태그에서 URL 찾기 (백업)
            if (url.isNullOrBlank()) {
                val guid = item.selectFirst("guid")?.text()?.trim()
                if (!guid.isNullOrBlank() && guid.startsWith("http")) {
                    url = guid
                }
            }

            url?.let {
                urls.add(it)
                if (urls.size >= limit) return urls
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