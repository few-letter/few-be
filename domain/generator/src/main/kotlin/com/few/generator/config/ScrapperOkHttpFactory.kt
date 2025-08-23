package com.few.generator.config

import io.github.oshai.kotlinlogging.KotlinLogging
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.brotli.BrotliInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

/**
 * 스크래퍼용 OkHttp 팩토리 설정
 */
@Configuration
class ScrapperOkHttpFactory {
    @Value("\${app.http.connect-timeout:10}")
    private val connectTimeout: Long = 10

    @Value("\${app.http.read-timeout:15}")
    private val readTimeout: Long = 15

    @Value("\${app.http.write-timeout:15}")
    private val writeTimeout: Long = 15

    @Value("\${app.http.max-idle-connections:1}")
    private val maxIdleConnections: Int = 1 // 최대 1개, 평소엔 0개 유지

    @Value("\${app.http.keep-alive-duration:60}")
    private val keepAliveDuration: Long = 60 // 60분 후 자동 해제

    private val log = KotlinLogging.logger {}

    /**
     * 스크래퍼용 OkHttpClient Bean
     * - 평소: 0개 연결 유지 (스케줄러 돌지 않을 경우 커넥션 0)
     * - 사용시: 최대 1개 연결 생성, Keep-Alive로 재사용
     * - 60분 후: 자동 해제
     */
    @Bean
    fun scrapperHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .connectionPool(
                ConnectionPool(
                    maxIdleConnections,
                    keepAliveDuration,
                    TimeUnit.MINUTES,
                ),
            ).connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .addInterceptor { chain ->
                val request =
                    chain
                        .request()
                        .newBuilder()
                        // 🤖 봇 탐지 우회 - 가장 중요!
                        .addHeader(
                            HttpHeaders.USER_AGENT,
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36",
                        )
                        // 📄 브라우저처럼 HTML 요청
                        .addHeader(
                            HttpHeaders.ACCEPT,
                            "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
                        )
                        // 🌐 한국어 우선, 다국어 지원
                        .addHeader(HttpHeaders.ACCEPT_LANGUAGE, "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                        // 🗜️ 압축 허용으로 전송 속도 향상
                        .addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br")
                        // 🔄 캐시 제어 - 최신 데이터 보장
                        .addHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
                        // 🔗 브라우저 업그레이드 요청 차단
                        .addHeader("Upgrade-Insecure-Requests", "1")
                        // 📱 같은 사이트 요청임을 알림 (CSRF 방어 우회)
                        .addHeader("Sec-Fetch-Dest", "document")
                        .addHeader("Sec-Fetch-Mode", "navigate")
                        .addHeader("Sec-Fetch-Site", "none")
                        .addHeader("Sec-Fetch-User", "?1")
                        // 🏃‍♂️ Keep-Alive 명시적 설정
                        .addHeader(HttpHeaders.CONNECTION, "keep-alive")
                        .build()
                chain.proceed(request)
            }.addNetworkInterceptor { chain ->
                val request = chain.request()
                val startTime = System.currentTimeMillis()

                val response = chain.proceed(request)
                val endTime = System.currentTimeMillis()

                log.info { "🌐 ${request.method} ${request.url} - ${response.code} (${endTime - startTime}ms)" }
                response
            }.addNetworkInterceptor { chain ->
                val response = chain.proceed(chain.request())

                // 압축된 응답 자동 해제
                val contentEncoding = response.header(HttpHeaders.CONTENT_ENCODING)
                val contentType = response.header(HttpHeaders.CONTENT_TYPE)

                if (contentEncoding == null || response.body == null) {
                    return@addNetworkInterceptor response
                }

                log.debug { "Content-Encoding: $contentEncoding, Content-Type: $contentType" }

                when (contentEncoding.lowercase()) {
                    "gzip" -> {
                        val decompressedBody =
                            GZIPInputStream(response.body!!.byteStream())
                                .readBytes()
                                .toResponseBody(response.body!!.contentType())
                        return@addNetworkInterceptor response
                            .newBuilder()
                            .removeHeader(HttpHeaders.CONTENT_ENCODING)
                            .body(decompressedBody)
                            .build()
                    }

                    "deflate" -> {
                        val decompressedBody =
                            InflaterInputStream(response.body!!.byteStream())
                                .readBytes()
                                .toResponseBody(response.body!!.contentType())
                        return@addNetworkInterceptor response
                            .newBuilder()
                            .removeHeader(HttpHeaders.CONTENT_ENCODING)
                            .body(decompressedBody)
                            .build()
                    }

                    "br" -> {
                        log.debug { "⚠️ Brotli 압축은 BrotliInterceptor 에서 처리" }
                        return@addNetworkInterceptor response
                    }

                    "identity" -> {
                        return@addNetworkInterceptor response
                    }

                    else -> {
                        response
                    }
                }
            }.addInterceptor(BrotliInterceptor)
            .build()
            .also {
                log.info {
                    "✅ OkHttpClient created OkHttpClient with connectTimeout=$connectTimeout, readTimeout=$readTimeout, writeTimeout=$writeTimeout, maxIdleConnections=$maxIdleConnections, keepAliveDuration=$keepAliveDuration"
                }
            }
    }
}