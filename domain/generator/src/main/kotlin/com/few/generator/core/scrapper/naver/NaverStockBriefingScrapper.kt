package com.few.generator.core.scrapper.naver

import com.google.gson.JsonParser
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Component
class NaverStockBriefingScrapper(
    private val scrapperHttpClient: OkHttpClient,
) {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val BASE_URL = "https://m.stock.naver.com/briefing/market/posts"
        private const val LISTING_API_URL = "https://m.stock.naver.com/front-api/market/briefing/list"
        private const val CONTENT_SELECTOR = "#content > div > article > div.ContentText_area-content__JVudc"
        private const val PARAGRAPH_SELECTOR = "p.BriefingSection_paragraph__cmBpR"
        private const val SOURCE_BADGE_SELECTOR = "button.ContentText_badge__y0sJi"
    }

    fun fetchLatestPostId(): Long? =
        try {
            val today =
                LocalDate
                    .now(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .toString()
            val url = "$LISTING_API_URL?date=$today&pageSize=50"
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
            log.warn(e) { "증시 브리핑 최신 postId 조회 실패: ${e.message}" }
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

        val rawContents = parseContent(html)

        log.info { "증시 브리핑 크롤링 완료 (postId=$postId): ${rawContents.size}개 항목" }
        return rawContents
    }

    /**
     * 증시 브리핑 상세 페이지 HTML에서 `<b>` 제목과
     * `p.BriefingSection_paragraph__cmBpR` 본문 쌍을 추출한다.
     */
    internal fun parseContent(html: String): List<StockBriefingRawContent> {
        val contentArea =
            Jsoup.parse(html).select(CONTENT_SELECTOR).firstOrNull()
                ?: run {
                    log.warn { "증시 브리핑 콘텐츠 영역을 찾을 수 없습니다" }
                    return emptyList()
                }
        return parseRawContents(contentArea)
    }

    /**
     * CONTENT_SELECTOR 하위를 순회하며 `<b>` 제목과 그 뒤에 이어지는
     * `p.BriefingSection_paragraph__cmBpR` 본문을 한 쌍으로 묶는다.
     * 본문 안의 출처 뱃지 버튼과 지수/종목 카드 등 다른 요소는 무시한다.
     */
    private fun parseRawContents(contentArea: Element): List<StockBriefingRawContent> {
        val rawContents = mutableListOf<StockBriefingRawContent>()
        var currentTitle: String? = null
        val currentBody = StringBuilder()

        fun flush() {
            val title = currentTitle
            if (title != null && currentBody.isNotBlank()) {
                rawContents.add(StockBriefingRawContent(title, currentBody.toString().trim()))
            }
        }

        contentArea.children().forEach { element ->
            if (element.tagName().equals("b", ignoreCase = true)) {
                flush()
                currentTitle = element.text().trim()
                currentBody.clear()
            } else if (currentTitle != null) {
                element.select(PARAGRAPH_SELECTOR).forEach { paragraph ->
                    paragraph.select(SOURCE_BADGE_SELECTOR).remove()
                    val text = paragraph.text().trim()
                    if (text.isNotBlank()) {
                        if (currentBody.isNotEmpty()) currentBody.append(" ")
                        currentBody.append(text)
                    }
                }
            }
        }
        flush()

        return rawContents
    }
}