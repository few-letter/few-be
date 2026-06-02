package com.few.generator.event.handler

import com.few.generator.event.ContentsSchedulingEvent
import com.few.generator.event.client.SlackWebhookClient
import com.few.web.client.Block
import com.few.web.client.SlackBodyProperty
import com.few.web.client.Text
import org.springframework.stereotype.Component

@Component
class ContentsSchedulingHandler(
    private val slackWebhookClient: SlackWebhookClient,
) {
    fun handle(event: ContentsSchedulingEvent) {
        if (event.isSuccess) return

        val body =
            SlackBodyProperty(
                blocks =
                    listOf(
                        Block(
                            type = "section",
                            text = Text(text = "📋 *작업*\n${event.title}"),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "✅ *isSuccess*\n${event.isSuccess}"),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "⏰ *시작 시간*\n${event.startTime}"),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "⌛ *전체 소요 시간* 🕐\n${event.totalTime}"),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "🔔 *message*\n${event.message}"),
                        ),
                        Block(
                            type = "section",
                            text = Text(text = "🚀 *result*\n${event.result}"),
                        ),
                    ),
            )

        slackWebhookClient.sendAsync(body)
    }
}