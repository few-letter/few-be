package com.few.generator.core.crawler

import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class NewsCrawler9to5mac(
    private val generatorRestTemplate: RestTemplate,
) : BaseNewsCrawler(generatorRestTemplate) {
    companion object {
        private const val PRESS_NAME = "9to5Mac"
        private const val BASE_URL = "https://9to5mac.com/page/%d/"
        private const val MAX_PAGES = 5
    }

    override fun getAvailableTopics(): List<String> = listOf("apple")

    override fun getNewsUrls(
        topic: String,
        limit: Int,
    ): List<String> {
        logger.info { "9to5Mac 뉴스 링크를 수집합니다." }
        val allArticles = mutableListOf<String>()

        for (page in 1..MAX_PAGES) {
            val url = BASE_URL.format(page)
            val articles = fetchPage(url)
            allArticles.addAll(articles)

            if (allArticles.size >= limit) break
        }

        return allArticles.take(limit)
    }

    private fun fetchPage(url: String): List<String> {
        val articles = mutableListOf<String>()
        val soup: Document? = getSoup(url) // 부모 클래스의 `getSoup()` 사용

        if (soup != null) {
            val articleElements: Elements = soup.select("article h2 a")
            for (article in articleElements) {
                article.attr("href")?.let { articles.add(it) }
            }
        }

        return articles
    }
}