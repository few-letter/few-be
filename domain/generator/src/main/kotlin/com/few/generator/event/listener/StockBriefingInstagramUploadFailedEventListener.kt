package com.few.generator.event.listener

import com.few.generator.event.StockBriefingInstagramUploadCompletedEvent
import com.few.generator.event.client.SlackWebhookClient
import com.few.web.client.Block
import com.few.web.client.SlackBodyProperty
import com.few.web.client.Text
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class StockBriefingInstagramUploadFailedEventListener(
    private val slackWebhookClient: SlackWebhookClient,
    private val notificationIoCoroutineScope: CoroutineScope,
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun handleEvent(event: StockBriefingInstagramUploadCompletedEvent) {
        if (event.success) return

        notificationIoCoroutineScope.launch {
            log.info { "증시 브리핑 실패 감지 (postId=${event.postId}, 단계=${event.failedStage}), Slack 알림 발송 시작" }

            try {
                sendFailureSlackNotification(event)
                log.info { "증시 브리핑 실패 Slack 알림 발송 완료 (postId=${event.postId})" }
            } catch (e: Exception) {
                log.error(e) { "증시 브리핑 실패 Slack 알림 발송 실패" }
            }
        }
    }

    private fun sendFailureSlackNotification(event: StockBriefingInstagramUploadCompletedEvent) {
        val timeStr = event.uploadTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

        val blocks =
            buildList {
                add(
                    Block(
                        type = "section",
                        text =
                            Text(
                                type = "mrkdwn",
                                text = ":x: *증시 브리핑 처리 실패*",
                            ),
                    ),
                )
                add(Block(type = "divider"))
                add(
                    Block(
                        type = "section",
                        text =
                            Text(
                                type = "mrkdwn",
                                text =
                                    buildString {
                                        appendLine("*실패 정보*")
                                        appendLine("• Post ID: *${event.postId}*")
                                        appendLine("• 실패 단계: *${event.failedStage ?: "알 수 없음"}*")
                                        appendLine("• 발생 시간: $timeStr")
                                    },
                            ),
                    ),
                )
                if (event.errorMessage != null) {
                    add(Block(type = "divider"))
                    add(
                        Block(
                            type = "section",
                            text =
                                Text(
                                    type = "mrkdwn",
                                    text = ":warning: *오류 메시지:*\n```${event.errorMessage}```",
                                ),
                        ),
                    )
                }
            }

        slackWebhookClient.sendAsync(SlackBodyProperty(blocks = blocks))
    }
}