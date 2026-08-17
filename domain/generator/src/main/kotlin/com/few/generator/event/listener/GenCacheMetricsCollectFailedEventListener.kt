package com.few.generator.event.listener

import com.few.generator.event.GenCacheMetricsCollectFailedEvent
import com.few.generator.event.client.SlackWebhookClient
import com.few.web.client.Block
import com.few.web.client.SlackBodyProperty
import com.few.web.client.Text
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class GenCacheMetricsCollectFailedEventListener(
    private val slackWebhookClient: SlackWebhookClient,
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun handleEvent(event: GenCacheMetricsCollectFailedEvent) {
        log.info { "GenCache 메트릭 수집 실패 감지, Slack 알림 발송 시작" }
        try {
            sendFailureSlackNotification(event)
            log.info { "GenCache 메트릭 수집 실패 Slack 알림 발송 완료" }
        } catch (e: Exception) {
            log.error(e) { "GenCache 메트릭 수집 실패 Slack 알림 발송 실패" }
        }
    }

    private fun sendFailureSlackNotification(event: GenCacheMetricsCollectFailedEvent) {
        val blocks =
            listOf(
                Block(
                    type = "section",
                    text =
                        Text(
                            type = "mrkdwn",
                            text = ":x: *GenCache Metrics Report Error*",
                        ),
                ),
                Block(
                    type = "section",
                    text =
                        Text(
                            type = "mrkdwn",
                            text = "*Date:* ${event.date}\n*Error:* ${event.errorMessage}",
                        ),
                ),
            )

        slackWebhookClient.sendAsync(SlackBodyProperty(blocks = blocks))
    }
}