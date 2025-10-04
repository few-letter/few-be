package com.few.generator.event.handler

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.event.dto.UnsubscribeEventDto
import com.few.web.client.Block
import com.few.web.client.SlackBodyProperty
import com.few.web.client.Text
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class UnsubscribeHandler(
    private val slackHttpClient: HttpClient,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
    @Value("\${urls.webhook.slack}") private val webhookUrl: String,
) {
    private val log = KotlinLogging.logger {}

    fun handle(event: UnsubscribeEventDto) {
        val body =
            SlackBodyProperty(
                blocks =
                    listOf(
                        Block(
                            type = "section",
                            text = Text(text = "📧 *구독 취소*\n이메일: ${event.email}"),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "🏷️ *카테고리*\n${event.categories}"),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "⏰ *취소 시간*\n${event.unsubscribedAt}"),
                        ),
                    ),
            )

        val request =
            HttpRequest
                .newBuilder()
                .timeout(java.time.Duration.ofSeconds(10))
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build()

        slackHttpClient
            .sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenAccept { response ->
                if (response.statusCode() in 200..299) {
                    log.info { "Webhook success: ${response.statusCode()}" }
                } else {
                    log.error { "Webhook failed: ${response.statusCode()}" }
                }
            }.exceptionally { e ->
                log.error(e) { "Webhook request failed" }
                null
            }
    }
}