package com.few.generator.event.handler

import com.few.generator.event.dto.UnsubscribeEventDto
import com.few.web.client.Block
import com.few.web.client.SlackBodyProperty
import com.few.web.client.Text
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class UnsubscribeHandler(
    private val webhookRestTemplate: RestTemplate,
    @Value("\${urls.webhook.slack}") private val webhookUrl: String,
) {
    private val log = KotlinLogging.logger {}

    suspend fun handle(event: UnsubscribeEventDto) {
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

        webhookRestTemplate
            .exchange(
                webhookUrl,
                HttpMethod.POST,
                HttpEntity(body),
                String::class.java,
            ).let { res ->
                if (res.statusCode.is2xxSuccessful) {
                    log.info { "Webhook success: ${res.statusCode}" }
                } else {
                    log.error { "Webhook failed: ${res.statusCode}" }
                }
            }
    }
}