package com.few.generator.core.crawler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NewsCrawlerBBC(
    private val generatorRestTemplate: RestTemplate,
) : BaseNewsCrawler(generatorRestTemplate) {
    companion object {
        private const val PRESS_NAME = "BBC"

        private val RSS_URLS =
            mapOf(
                "world" to "http://feeds.bbci.co.uk/news/world/rss.xml",
                "uk" to "http://feeds.bbci.co.uk/news/uk/rss.xml",
                "business" to "http://feeds.bbci.co.uk/news/business/rss.xml",
                "politics" to "http://feeds.bbci.co.uk/news/politics/rss.xml",
                "health" to "http://feeds.bbci.co.uk/news/health/rss.xml",
                "science" to "http://feeds.bbci.co.uk/news/science_and_environment/rss.xml",
                "technology" to "http://feeds.bbci.co.uk/news/technology/rss.xml",
                "entertainment" to "http://feeds.bbci.co.uk/news/entertainment_and_arts/rss.xml",
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
            logger.warn { "Topic $topic not found in BBC RSS feeds" }
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
                // 1. link 태그에서 URL 찾기
                val link = item.selectFirst("link")?.text()?.trim()
                if (!link.isNullOrBlank()) {
                    urls.add(link)
                    if (urls.size >= limit) break
                    continue
                }

                // 2. guid 태그에서 URL 찾기 (백업)
                val guid = item.selectFirst("guid")?.text()?.trim()
                if (!guid.isNullOrBlank() && guid.startsWith("http")) {
                    val cleanedUrl = guid.split("#")[0] // '#' 이후 부분 제거
                    urls.add(cleanedUrl)
                    if (urls.size >= limit) break
                }
            }

            logger.info { "Found ${urls.size} URLs for BBC topic $topic" }
        } catch (e: Exception) {
            logger.error { "Error processing BBC RSS feed for $topic: ${e.message}" }
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