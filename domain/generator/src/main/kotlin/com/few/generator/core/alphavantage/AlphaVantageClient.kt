package com.few.generator.core.alphavantage

import com.few.generator.config.properties.AlphaVantageProperties
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Component
class AlphaVantageClient(
    private val alphaVantageHttpClient: HttpClient,
    private val alphaVantageProperties: AlphaVantageProperties,
) {
    private val log = KotlinLogging.logger {}
    private val gson: Gson = GsonBuilder().create()

    fun getTopNewsFeed(ticker: String): List<AlphaVantageNewsFeed> {
        if (alphaVantageProperties.apiKey.isBlank()) {
            log.warn { "AlphaVantage API Key가 설정되지 않았습니다. ticker=$ticker" }
            throw RuntimeException("AlphaVantage API Key가 설정되지 않았습니다.")
        }

        val url = "${alphaVantageProperties.baseUrl}?function=NEWS_SENTIMENT&tickers=$ticker&apikey=${alphaVantageProperties.apiKey}"
        log.info { "AlphaVantage 뉴스 조회 시작: ticker=$ticker" }

        val request =
            HttpRequest
                .newBuilder()
                .timeout(Duration.ofSeconds(15))
                .uri(URI.create(url))
                .GET()
                .build()

        val response = alphaVantageHttpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw RuntimeException("AlphaVantage HTTP ${response.statusCode()}: ticker=$ticker")
        }

        val parsed = gson.fromJson(response.body(), AlphaVantageNewsResponse::class.java)

        parsed.information?.let {
            if (parsed.feed.isNullOrEmpty()) {
                throw RuntimeException("AlphaVantage API 에러 발생: ${parsed.information}")
            }
        }

        val feed = parsed.feed?.take(alphaVantageProperties.topFeedCount) ?: emptyList()

        log.info { "AlphaVantage 뉴스 조회 완료: ticker=$ticker, ${feed.size}개 항목" }
        return feed
    }

    private data class AlphaVantageNewsResponse(
        val items: String?,
        @SerializedName("feed") val feed: List<AlphaVantageNewsFeed>?,
        @SerializedName("Information") val information: String?,
    )
}