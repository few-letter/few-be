package com.few.generator.event.client

import com.few.generator.config.GeneratorGsonConfig
import com.few.web.client.SlackBodyProperty
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Component
class SlackWebhookClient(
    private val slackHttpClient: HttpClient,
    @Qualifier(GeneratorGsonConfig.Companion.GSON_BEAN_NAME) private val gson: Gson,
    @Value("\${urls.webhook.slack}") private val webhookUrl: String,
) {
    private val log = KotlinLogging.logger {}

    fun sendAsync(bodyObj: SlackBodyProperty) {
        val request =
            HttpRequest
                .newBuilder()
                .timeout(Duration.ofSeconds(10))
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(bodyObj)))
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