package com.few.generator.core.alphavantage

import com.few.generator.config.AlphaVantageProperties
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component

@Component
class AlphaVantageClient(
    private val scrapperHttpClient: OkHttpClient,
    private val alphaVantageProperties: AlphaVantageProperties,
) {
    private val log = KotlinLogging.logger {}
    private val gson: Gson = GsonBuilder().create()

    companion object {
        private const val BASE_URL = "https://www.alphavantage.co/query"
        private const val TOP_FEED_COUNT = 4
    }

    fun getTopNewsFeed(ticker: String): List<AlphaVantageNewsFeedItem> {
        val url = "$BASE_URL?function=NEWS_SENTIMENT&tickers=$ticker&apikey=${alphaVantageProperties.apiKey}"
        log.info { "AlphaVantage 뉴스 조회 시작: ticker=$ticker" }

        val request = Request.Builder().url(url).build()
        val body =
            scrapperHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RuntimeException("AlphaVantage HTTP ${response.code} ${response.message}: ticker=$ticker")
                }
                response.body?.string() ?: throw RuntimeException("AlphaVantage 응답 본문 없음: ticker=$ticker")
            }

        val response = gson.fromJson(body, AlphaVantageNewsResponse::class.java)
        val feed = response.feed?.take(TOP_FEED_COUNT) ?: emptyList()

        log.info { "AlphaVantage 뉴스 조회 완료: ticker=$ticker, ${feed.size}개 항목" }
        return feed
    }

    private data class AlphaVantageNewsResponse(
        val items: String?,
        @SerializedName("feed") val feed: List<AlphaVantageNewsFeedItem>?,
    )
}