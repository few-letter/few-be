package com.few.generator.core.scrapper.naver

import com.google.gson.JsonParser
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.springframework.stereotype.Component

@Component
class NaverStockBriefingScrapper(
    private val scrapperHttpClient: OkHttpClient,
) {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val BASE_URL = "https://m.stock.naver.com/briefing/market/posts"
        private const val LISTING_API_URL = "https://m.stock.naver.com/front-api/market/briefing/list"
        private const val CONTENT_SELECTOR = "#content > div > article > div.ContentText_area-content__JVudc"
    }

    fun fetchLatestPostId(date: String): Long? =
        try {
            val url = "$LISTING_API_URL?date=$date&pageSize=50"
            val request = Request.Builder().url(url).build()
            val responseBody =
                scrapperHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        log.warn { "증시 브리핑 목록 API HTTP ${response.code}: $url" }
                        return null
                    }
                    response.body?.string() ?: return null
                }
            JsonParser
                .parseString(responseBody)
                .asJsonObject
                .getAsJsonObject("result")
                ?.getAsJsonArray("items")
                ?.firstOrNull()
                ?.asJsonObject
                ?.get("id")
                ?.asLong
        } catch (e: Exception) {
            log.warn(e) { "증시 브리핑 최신 postId 조회 실패 (date=$date): ${e.message}" }
            null
        }

    fun checkPostExists(postId: Long): Boolean =
        try {
            val request = Request.Builder().url("$BASE_URL/$postId").build()
            scrapperHttpClient.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            log.warn(e) { "증시 브리핑 포스트 존재 확인 실패 (postId=$postId): ${e.message}" }
            false
        }

    fun scrapePost(postId: Long): List<StockBriefingRawContent> {
        val url = "$BASE_URL/$postId"
        log.info { "증시 브리핑 크롤링 시작: $url" }

        val request = Request.Builder().url(url).build()
        val html =
            scrapperHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RuntimeException("증시 브리핑 HTTP ${response.code} ${response.message}: $url")
                }
                response.body?.string() ?: throw RuntimeException("증시 브리핑 응답 본문 없음: $url")
            }

        val document = Jsoup.parse(html)
        val contentArea =
            document.select(CONTENT_SELECTOR).firstOrNull()
                ?: run {
                    log.warn { "증시 브리핑 콘텐츠 영역을 찾을 수 없습니다 (postId=$postId)" }
                    return emptyList()
                }

        val rawContents = mutableListOf<StockBriefingRawContent>()
        var currentTitle: String? = null
        val currentBody = StringBuilder()

        contentArea.children().forEach { element ->
            when (element.tagName().lowercase()) {
                "b" -> {
                    if (currentTitle != null && currentBody.isNotBlank()) {
                        rawContents.add(StockBriefingRawContent(currentTitle!!, currentBody.toString().trim()))
                        currentBody.clear()
                    }
                    currentTitle = element.text().trim()
                    currentBody.clear()
                }
                "p" -> {
                    val text = element.text().trim()
                    if (text.isNotBlank() && currentTitle != null) {
                        if (currentBody.isNotEmpty()) currentBody.append(" ")
                        currentBody.append(text)
                    }
                }
            }
        }

        if (currentTitle != null && currentBody.isNotBlank()) {
            rawContents.add(StockBriefingRawContent(currentTitle!!, currentBody.toString().trim()))
        }

        log.info { "증시 브리핑 크롤링 완료 (postId=$postId): ${rawContents.size}개 항목" }
        return rawContents
    }
}