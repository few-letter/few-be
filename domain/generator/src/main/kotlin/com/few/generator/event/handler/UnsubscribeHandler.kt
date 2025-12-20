package com.few.generator.event.handler

import com.few.generator.event.client.SlackWebhookClient
import com.few.generator.event.dto.UnsubscribeEventDto
import com.few.web.client.Block
import com.few.web.client.SlackBodyProperty
import com.few.web.client.Text
import org.springframework.stereotype.Component

@Component
class UnsubscribeHandler(
    private val slackWebhookClient: SlackWebhookClient,
) {
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
                        Block(
                            type = "section",
                            text = Text(text = "🔔 *구독 컨텐츠 종류*\n${event.contentsType.title}"),
                        ),
                    ),
            )

        slackWebhookClient.sendAsync(body)
    }
}