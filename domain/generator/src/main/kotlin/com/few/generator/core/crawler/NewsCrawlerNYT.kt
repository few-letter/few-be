package com.few.generator.core.crawler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NewsCrawlerNYT(
    private val generatorRestTemplate: RestTemplate,
) : BaseNewsCrawler(generatorRestTemplate) {
    companion object {
        private val RSS_URLS =
            mapOf(
                "world" to "https://rss.nytimes.com/services/xml/rss/nyt/World.xml",
                "business" to "https://rss.nytimes.com/services/xml/rss/nyt/Business.xml",
                "technology" to "https://rss.nytimes.com/services/xml/rss/nyt/Technology.xml",
                "science" to "https://rss.nytimes.com/services/xml/rss/nyt/Science.xml",
                "health" to "https://rss.nytimes.com/services/xml/rss/nyt/Health.xml",
                "sports" to "https://rss.nytimes.com/services/xml/rss/nyt/Sports.xml",
                "arts" to "https://rss.nytimes.com/services/xml/rss/nyt/Arts.xml",
                "books" to "https://rss.nytimes.com/services/xml/rss/nyt/Books.xml",
                "style" to "https://rss.nytimes.com/services/xml/rss/nyt/Style.xml",
                "travel" to "https://rss.nytimes.com/services/xml/rss/nyt/Travel.xml",
                "magazine" to "https://rss.nytimes.com/services/xml/rss/nyt/Magazine.xml",
            )
    }

    override fun getAvailableTopics(): List<String> = RSS_URLS.keys.toList()

    override fun getNewsUrls(
        topic: String,
        limit: Int,
    ): List<String> {
        val urls = mutableListOf<String>()
        val rssUrl = RSS_URLS[topic]

        if (rssUrl == null) {
            logger.warn { "Topic $topic not found in NYT RSS feeds" }
            return urls
        }

        try {
            val soup: Document? = getSoup(rssUrl)
            if (soup == null) {
                logger.error { "Failed to fetch RSS feed for topic $topic" }
                return urls
            }

            var items = soup.select("item")
            if (items.isEmpty()) {
                items = soup.select("entry") // item이 없으면 entry 태그 찾기 시도
            }

            for (item in items) {
                var url: String? = null

                // 1. 일반적인 link 태그 확인
                val link = item.selectFirst("link")
                if (link != null) {
                    url = link.text()?.trim()
                    if (url.isNullOrBlank()) {
                        url = link.attr("href")?.trim()
                        if (url.isNullOrBlank()) {
                            val nextSibling = link.nextSibling()?.toString()?.trim()
                            if (!nextSibling.isNullOrBlank() && nextSibling.startsWith("http")) {
                                url = nextSibling
                            }
                        }
                    }
                }

                // 2. atom:link 태그 확인
                if (url.isNullOrBlank()) {
                    val atomLink = item.selectFirst("atom:link")?.attr("href")?.trim()
                    if (!atomLink.isNullOrBlank()) {
                        url = atomLink
                    }
                }

                // 3. guid 태그 확인
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

            logger.info { "Found ${urls.size} URLs for NYT topic $topic" }
        } catch (e: Exception) {
            logger.error { "Error processing NYT RSS feed for $topic: ${e.message}" }
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