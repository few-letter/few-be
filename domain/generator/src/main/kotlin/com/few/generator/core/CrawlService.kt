package com.few.generator.core

import com.few.generator.core.crawler.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class CrawlService(
    private val crawlers: List<BaseNewsCrawler>,
) {
    private val logger = KotlinLogging.logger {}

    fun crawl(
        topic: String,
        limit: Int = 10,
    ): List<String> =
        try {
            _crawl(topic, limit)
        } catch (e: Exception) {
            logger.error { "크롤링 중 오류 발생: ${e.message}" }
            emptyList()
        }

    private fun _crawl(
        topic: String?,
        limit: Int,
    ): List<String> {
        val selectedCrawlers = crawlers.filter { topic == null || it.getAvailableTopics().contains(topic) }
        if (selectedCrawlers.isEmpty()) return emptyList()

        val uniqueUrls = mutableSetOf<String>()
        var successfulCrawlers = mutableListOf<BaseNewsCrawler>()
        var roundNo = 0
        var perCrawler = maxOf(1, limit / selectedCrawlers.size)

        while (uniqueUrls.size < limit && (selectedCrawlers.isNotEmpty() || successfulCrawlers.isNotEmpty()) && roundNo < 3) {
            val targetCrawlers = if (roundNo == 0) selectedCrawlers else successfulCrawlers
            val results = targetCrawlers.associateWith { it.getNewsUrls(topic ?: "", perCrawler) }

            successfulCrawlers = mutableListOf()
            var added = 0

            results.forEach { (crawler, result) ->
                if (result.isEmpty()) {
                    logger.error { "${crawler::class.simpleName} 크롤링 실패" }
                } else {
                    val newUrls = result.toSet() - uniqueUrls
                    if (newUrls.isNotEmpty()) {
                        uniqueUrls.addAll(newUrls)
                        added += newUrls.size
                    }
                    successfulCrawlers.add(crawler)
                    logger.info { "${crawler::class.simpleName}에서 ${newUrls.size}개의 새로운 URL 수집 (총 ${result.size}개)." }
                }
            }

            val remaining = limit - uniqueUrls.size
            perCrawler = maxOf(1, remaining / maxOf(successfulCrawlers.size, 1))

            if (added == 0) break
            roundNo++
        }

        if (uniqueUrls.size < limit) {
            logger.warn { "요청한 ${limit}개 중 ${uniqueUrls.size}개만 수집됨." }
        }
        return uniqueUrls.take(limit)
    }
}