package com.few.generator.event.listener

import com.few.generator.event.InstagramTokenRefreshFailedEvent
import com.few.generator.event.client.SlackWebhookClient
import com.few.web.client.Block
import com.few.web.client.SlackBodyProperty
import com.few.web.client.Text
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class InstagramTokenRefreshFailedEventListener(
    private val slackWebhookClient: SlackWebhookClient,
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun handleEvent(event: InstagramTokenRefreshFailedEvent) {
        log.info { "Instagram 토큰 갱신 실패 감지, Slack 알림 발송 시작" }
        try {
            sendFailureSlackNotification(event)
            log.info { "Instagram 토큰 갱신 실패 Slack 알림 발송 완료" }
        } catch (e: Exception) {
            log.error(e) { "Instagram 토큰 갱신 실패 Slack 알림 발송 실패" }
        }
    }

    private fun sendFailureSlackNotification(event: InstagramTokenRefreshFailedEvent) {
        val timeStr = event.occurredAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val blocks =
            listOf(
                Block(
                    type = "section",
                    text =
                        Text(
                            type = "mrkdwn",
                            text = ":x: *Instagram 토큰 갱신 실패*",
                        ),
                ),
                Block(type = "divider"),
                Block(
                    type = "section",
                    text =
                        Text(
                            type = "mrkdwn",
                            text = "*발생 시간:* $timeStr\n*오류 메시지:*\n```${event.errorMessage}```",
                        ),
                ),
            )

        slackWebhookClient.sendAsync(SlackBodyProperty(blocks = blocks))
    }
}