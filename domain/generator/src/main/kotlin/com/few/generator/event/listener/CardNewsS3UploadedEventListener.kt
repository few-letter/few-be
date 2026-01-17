package com.few.generator.event.listener

import com.few.generator.event.CardNewsS3UploadedEvent
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
class CardNewsS3UploadedEventListener(
    private val slackWebhookClient: SlackWebhookClient,
    private val notificationIoCoroutineScope: CoroutineScope,
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun handleEvent(event: CardNewsS3UploadedEvent) {
        notificationIoCoroutineScope.launch {
            log.info { "${event.region.name} 카드뉴스 S3 업로드 완료 감지, Slack 알림 발송 시작" }

            try {
                sendSlackNotification(event)
                log.info { "${event.region.name} 카드뉴스 S3 업로드 Slack 알림 발송 완료" }
            } catch (e: Exception) {
                log.error(e) { "${event.region.name} Slack 알림 발송 실패" }
            }
        }
    }

    private fun sendSlackNotification(event: CardNewsS3UploadedEvent) {
        val successRate =
            if (event.totalCount > 0) {
                (event.uploadedCount.toDouble() / event.totalCount * 100)
            } else {
                0.0
            }

        val emoji =
            when {
                successRate == 100.0 -> ":rocket:"
                successRate >= 90.0 -> ":white_check_mark:"
                successRate >= 70.0 -> ":warning:"
                else -> ":x:"
            }

        val statusMessage =
            when {
                successRate == 100.0 -> "Perfect! All images uploaded successfully"
                successRate >= 90.0 -> "Great! Most images uploaded"
                successRate >= 70.0 -> "Partial success - some images failed"
                else -> "Upload failed - needs attention"
            }

        val blocks =
            buildList {
                add(
                    Block(
                        type = "section",
                        text =
                            Text(
                                type = "mrkdwn",
                                text = "$emoji *Card News S3 Upload Complete*",
                            ),
                    ),
                )
                add(
                    Block(
                        type = "section",
                        text =
                            Text(
                                type = "mrkdwn",
                                text = "*Region:* ${event.region.name}",
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
                                        appendLine("*Upload Summary*")
                                        appendLine("• Uploaded: *${event.uploadedCount}* / ${event.totalCount} images")
                                        appendLine("• Success Rate: *${String.format("%.1f%%", successRate)}*")
                                        appendLine(
                                            "• Upload Time: ${event.uploadTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}",
                                        )
                                    },
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
                                text = "$emoji *Status:* $statusMessage",
                            ),
                    ),
                )

                // 에러 메시지가 있으면 추가
                event.errorMessage?.let { error ->
                    add(Block(type = "divider"))
                    add(
                        Block(
                            type = "section",
                            text =
                                Text(
                                    type = "mrkdwn",
                                    text = ":warning: *Error Details:*\n```$error```",
                                ),
                        ),
                    )
                }
            }

        val slackBody = SlackBodyProperty(blocks = blocks)
        slackWebhookClient.sendAsync(slackBody)
    }
}