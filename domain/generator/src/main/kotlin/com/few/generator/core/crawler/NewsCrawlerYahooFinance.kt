package com.few.generator.core.crawler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NewsCrawlerYahooFinance(
    private val generatorRestTemplate: RestTemplate,
) : BaseNewsCrawler(generatorRestTemplate) {
    companion object {
        private const val PRESS_NAME = "Yahoo Finance"

        private val RSS_URLS =
            mapOf(
                "finance" to "https://finance.yahoo.com/news/rss",
                "market" to "https://finance.yahoo.com/rss/market",
                "business" to "https://finance.yahoo.com/rss/business",
                "economy" to "https://finance.yahoo.com/rss/economy",
                "technology" to "https://finance.yahoo.com/rss/technology",
                "stocks" to "https://finance.yahoo.com/rss/stocks",
                "personal_finance" to "https://finance.yahoo.com/rss/personal-finance",
                "crypto" to "https://finance.yahoo.com/rss/crypto",
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
            logger.warn { "Topic $topic not found in Yahoo Finance RSS feeds" }
            return urls
        }

        val soup: Document? = getSoup(rssUrl)
        if (soup == null) {
            logger.error { "Failed to fetch RSS feed for topic $topic" }
            return urls
        }

        val items = soup.select("item")
        for (item in items) {
            val link = item.selectFirst("link")?.text()?.trim()
            if (!link.isNullOrBlank() && link.startsWith("https://finance.yahoo.com/")) {
                urls.add(link)
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