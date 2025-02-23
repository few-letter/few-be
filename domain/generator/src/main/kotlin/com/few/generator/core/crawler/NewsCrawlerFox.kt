package com.few.generator.core.crawler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NewsCrawlerFox(
    private val generatorRestTemplate: RestTemplate,
) : BaseNewsCrawler(generatorRestTemplate) {
    companion object {
        private val RSS_URLS =
            mapOf(
                "latest" to "https://moxie.foxnews.com/google-publisher/latest.xml",
                "world" to "https://moxie.foxnews.com/google-publisher/world.xml",
                "politics" to "https://moxie.foxnews.com/google-publisher/politics.xml",
                "science" to "https://moxie.foxnews.com/google-publisher/science.xml",
                "health" to "https://moxie.foxnews.com/google-publisher/health.xml",
                "sports" to "https://moxie.foxnews.com/google-publisher/sports.xml",
                "travel" to "https://moxie.foxnews.com/google-publisher/travel.xml",
                "technology" to "https://moxie.foxnews.com/google-publisher/tech.xml",
                "opinion" to "https://moxie.foxnews.com/google-publisher/opinion.xml",
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
            logger.warn { "Topic $topic not found in Fox News RSS feeds" }
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
                val link = item.selectFirst("link")?.text()?.trim()
                if (!link.isNullOrBlank()) {
                    url = link
                }

                // 2. guid 태그에서 URL 찾기
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

            logger.info { "Found ${urls.size} URLs for Fox News topic $topic" }
        } catch (e: Exception) {
            logger.error { "Error processing Fox News RSS feed for $topic: ${e.message}" }
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