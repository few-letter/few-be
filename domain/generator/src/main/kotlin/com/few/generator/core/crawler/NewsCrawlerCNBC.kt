package com.few.generator.core.crawler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NewsCrawlerCNBC(
    private val generatorRestTemplate: RestTemplate,
) : BaseNewsCrawler(generatorRestTemplate) {
    companion object {
        private const val PRESS_NAME = "CNBC"

        private val RSS_URLS =
            mapOf(
                "top" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=100003114",
                "world" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=100727362",
                "us" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=15837362",
                "asia" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=19832390",
                "europe" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=19794221",
                "business" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10001147",
                "earnings" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=15839135",
                "commentary" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=100370673",
                "economy" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=20910258",
                "finance" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000664",
                "technology" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=19854910",
                "politics" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000113",
                "health" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000108",
                "realestate" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000115",
                "wealth" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10001054",
                "autos" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000101",
                "energy" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=19836768",
                "media" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000110",
                "retail" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000116",
                "travel" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=10000739",
                "smallbusiness" to "https://search.cnbc.com/rs/search/combinedcms/view.xml?partnerId=wrss01&id=44877279",
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
            logger.warn { "Topic $topic not found in CNBC RSS feeds" }
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
                if (!link.isNullOrBlank() && link.startsWith("https://www.cnbc.com/")) {
                    urls.add(link)
                    if (urls.size >= limit) break
                    continue
                }

                // 2. metadata:id와 metadata:type을 확인하여 유효한 뉴스 기사인지 확인
                val metadataType = item.selectFirst("metadata[type]")?.text()?.trim()
                if (metadataType == "cnbcnewsstory") {
                    val metadataId = item.selectFirst("metadata[id]")?.text()?.trim()
                    if (!metadataId.isNullOrBlank()) {
                        urls.add("https://www.cnbc.com/id/$metadataId")
                        if (urls.size >= limit) break
                    }
                }
            }

            logger.info { "Found ${urls.size} URLs for CNBC topic $topic" }
        } catch (e: Exception) {
            logger.error { "Error processing CNBC RSS feed for $topic: ${e.message}" }
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