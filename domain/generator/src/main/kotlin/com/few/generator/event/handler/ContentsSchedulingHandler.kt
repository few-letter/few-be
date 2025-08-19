package com.few.generator.event.handler

import com.few.generator.event.dto.ContentsSchedulingEventDto
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
class ContentsSchedulingHandler(
    private val webhookRestTemplate: RestTemplate,
    @Value("\${urls.webhook.slack}") private val webhookUrl: String,
) {
    private val log = KotlinLogging.logger {}

    suspend fun handle(event: ContentsSchedulingEventDto) {
        val body =
            SlackBodyProperty(
                blocks =
                    listOf(
                        Block(
                            type = "section",
                            text = Text(text = "✅ *isSuccess*\n" + event.isSuccess.toString()),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "⏰ *시작 시간*\n" + event.startTime.toString()),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "⌛ *전체 소요 시간* 🕐\n" + event.totalTime),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "🔔 *message*\n" + event.message),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "🚀 *result*\n" + event.result),
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