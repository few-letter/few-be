package com.few.generator.event.handler

import com.few.generator.event.EnrollSubscriptionEvent
import com.few.generator.event.client.SlackWebhookClient
import com.few.web.client.Block
import com.few.web.client.SlackBodyProperty
import com.few.web.client.Text
import org.springframework.stereotype.Component

@Component
class EnrollSubscriptionHandler(
    private val slackWebhookClient: SlackWebhookClient,
) {
    fun handle(event: EnrollSubscriptionEvent) {
        val body =
            SlackBodyProperty(
                blocks =
                    listOf(
                        Block(
                            type = "section",
                            text = Text(text = "📧 *신규 구독*\n이메일: ${event.email}"),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "🏷️ *카테고리*\n${event.categories}"),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "⏰ *구독 시간*\n${event.enrolledAt}"),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "🔔 *구독 컨텐츠 종류*\n${event.contentsType.title}"),
                        ),
                    ),
            )

        slackWebhookClient.sendAsync(body)
    }
}