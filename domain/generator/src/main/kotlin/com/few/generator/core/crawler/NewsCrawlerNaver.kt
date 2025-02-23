package com.few.generator.core.crawler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NewsCrawlerNaver(
    private val generatorRestTemplate: RestTemplate,
) : BaseNewsCrawler(generatorRestTemplate) {
    companion object {
        private const val BASE_URL = "https://news.naver.com/main/main.naver"
        private val HEADERS =
            mapOf(
                "User-Agent" to
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
            )
        private val TOPIC_MAPPING =
            mapOf(
                "kr_politics" to 100,
                "kr_business" to 101,
                "kr_technology" to 105,
            )
    }

    override fun getAvailableTopics(): List<String> = TOPIC_MAPPING.keys.toList()

    override fun getNewsUrls(
        topic: String,
        limit: Int,
    ): List<String> {
        if (!TOPIC_MAPPING.containsKey(topic)) return emptyList()

        val sid = TOPIC_MAPPING[topic]!!
        val allLinks = mutableSetOf<String>()
        var page = 1

        while (allLinks.size < limit && page <= 10) {
            val url = "$BASE_URL?mode=LSD&mid=shm&sid1=$sid#&date=%2000:00:00&page=$page"
            val soup: Document? = getSoup(url) ?: break

            val naverLinks =
                soup
                    ?.select("a[href~=https://n\\.news\\.naver\\.com/mnews/article/\\d+/\\d+$]")
                    ?.mapNotNull { it.attr("href") }
                    ?.toSet()

            if (naverLinks != null) {
                for (naverLink in naverLinks) {
                    if (allLinks.size >= limit) break

                    val articleSoup: Document? = getSoup(naverLink)
                    articleSoup
                        ?.selectFirst(
                            "#ct > div.media_end_head.go_trans > div.media_end_head_info.nv_notrans > div.media_end_head_info_datestamp > a.media_end_head_origin_link",
                        )?.attr("href")
                        ?.let { allLinks.add(it) }
                }
            }

            page++
        }

        return allLinks.take(limit)
    }

    override fun getSoup(url: String): Document? {
        val html = getRequest(url)
        return try {
            html?.let { Jsoup.parse(it) }
        } catch (e: Exception) {
            logger.error { "Failed to parse HTML: ${e.message}" }
            null
        }
    }
}