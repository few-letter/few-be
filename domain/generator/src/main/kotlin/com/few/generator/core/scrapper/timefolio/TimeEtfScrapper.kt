package com.few.generator.core.scrapper.timefolio

import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class TimeEtfScrapper(
    private val scrapperHttpClient: OkHttpClient,
) {
    private val log = KotlinLogging.logger {}

    fun scrapeTopItems(): List<TimeEtfItem> {
        log.info { "TimeETF 구성 종목 크롤링 시작: ${TimeEtfConstants.URL}" }

        val request = Request.Builder().url(TimeEtfConstants.URL).build()
        val html =
            scrapperHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RuntimeException("TimeETF HTTP ${response.code} ${response.message}: ${TimeEtfConstants.URL}")
                }
                response.body?.string() ?: throw RuntimeException("TimeETF 응답 본문 없음: ${TimeEtfConstants.URL}")
            }

        val document = Jsoup.parse(html)
        val tbody =
            document.select(TimeEtfConstants.TBODY_SELECTOR).firstOrNull()
                ?: run {
                    log.warn { "TimeETF tbody를 찾을 수 없습니다." }
                    return emptyList()
                }

        val items =
            tbody.select("tr").take(TimeEtfConstants.TOP_ITEM_COUNT).mapIndexedNotNull { index, row ->
                val cells = row.select("td")
                if (cells.size < 2) return@mapIndexedNotNull null

                val ticker =
                    cells[0]
                        .text()
                        .trim()
                        .split(" ")
                        .firstOrNull()
                        ?.takeIf { it.isNotBlank() }
                        ?.takeIf { t -> TimeEtfConstants.EXCLUDED_TICKER_KEYWORDS.none { t.contains(it) } }
                        ?: return@mapIndexedNotNull null
                val stockName = cells[1].text().trim().takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null

                TimeEtfItem(
                    rank = index + 1,
                    ticker = ticker,
                    stockName = stockName,
                )
            }

        log.info { "TimeETF 구성 종목 크롤링 완료: ${items.size}개 항목" }
        return items
    }
}