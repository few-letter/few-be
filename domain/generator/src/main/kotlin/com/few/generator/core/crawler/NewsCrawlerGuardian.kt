package com.few.generator.core.crawler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NewsCrawlerGuardian(
    private val generatorRestTemplate: RestTemplate,
) : BaseNewsCrawler(generatorRestTemplate) {
    companion object {
        private val RSS_URLS =
            mapOf(
                "politics" to "https://www.theguardian.com/politics/rss",
                "coronavirus" to "https://www.theguardian.com/world/coronavirus-outbreak/rss",
                "world" to "https://www.theguardian.com/world/rss",
                "uk" to "https://www.theguardian.com/uk-news/rss",
                "europe" to "https://www.theguardian.com/world/europe-news/rss",
                "us" to "https://www.theguardian.com/us-news/rss",
                "americas" to "https://www.theguardian.com/world/americas/rss",
                "asia" to "https://www.theguardian.com/world/asia/rss",
                "australia" to "https://www.theguardian.com/australia-news/rss",
                "money" to "https://www.theguardian.com/money/rss",
                "middle-east" to "https://www.theguardian.com/world/middleeast/rss",
                "global-development" to "https://www.theguardian.com/global-development/rss",
                "sport" to "https://www.theguardian.com/sport/rss",
                "lifestyle" to "https://www.theguardian.com/lifeandstyle/rss",
                "technology" to "https://www.theguardian.com/technology/rss",
                "culture" to "https://www.theguardian.com/culture/rss",
                "environment" to "https://www.theguardian.com/environment/rss",
                "science" to "https://www.theguardian.com/science/rss",
                "business" to "https://www.theguardian.com/business/rss",
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
            logger.warn { "Topic $topic not found in Guardian RSS feeds" }
            return urls
        }

        try {
            val soup: Document? = getSoup(rssUrl)
            if (soup == null) {
                logger.error { "Failed to fetch RSS feed for topic $topic" }
                return urls
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

                url?.let {
                    urls.add(it)
                    if (urls.size >= limit) return urls
                }
            }
        } catch (e: Exception) {
            logger.error { "Error processing Guardian RSS feed for $topic: ${e.message}" }
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