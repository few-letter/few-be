package com.few.generator.core.connection

import com.few.generator.config.JsoupConnectionFactory
import common.exception.BadRequestException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jsoup.HttpStatusException
import org.jsoup.nodes.Document
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RetryableJsoup(
    private val connectionFactory: JsoupConnectionFactory,
    @Value("\${generator.scraping.maxRetries}")
    private val maxRetries: Int,
    @Value("\${generator.scraping.defaultRetryAfter}")
    private val defaultRetryAfter: Long,
) {
    private val log = KotlinLogging.logger {}

    fun connect(url: String): Document {
        var attempt = 0
        while (attempt < maxRetries) {
            try {
                return connectionFactory
                    .createConnection(url)
                    .execute()
                    .parse()
            } catch (e: Exception) {
                when (e) {
                    is HttpStatusException -> {
                        if (e.statusCode != 429) throw e
                        log.error { "URL($url) Response 429, attempt: $attempt" }
                        TimeUnit.SECONDS.sleep(defaultRetryAfter + attempt + (1..10).random())
                        attempt++
                        continue
                    }
                    else -> throw e
                }
            }
        }
        throw BadRequestException("Failed to fetch document after $maxRetries attempts for URL: $url")
    }
}