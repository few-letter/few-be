package com.few.generator.event.listener

import com.few.generator.event.InstagramUploadCompletedEvent
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
class InstagramUploadCompletedEventListener(
    private val slackWebhookClient: SlackWebhookClient,
    private val notificationIoCoroutineScope: CoroutineScope,
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun handleEvent(event: InstagramUploadCompletedEvent) {
        notificationIoCoroutineScope.launch {
            log.info { "${event.region.name} Instagram 업로드 완료 감지, Slack 알림 발송 시작" }

            try {
                sendSlackNotification(event)
                log.info { "${event.region.name} Instagram 업로드 Slack 알림 발송 완료" }
            } catch (e: Exception) {
                log.error(e) { "${event.region.name} Instagram 업로드 Slack 알림 발송 실패" }
            }
        }
    }

    private fun sendSlackNotification(event: InstagramUploadCompletedEvent) {
        val totalCount = event.successCategories.size + event.failedCategories.size
        val successRate =
            if (totalCount > 0) {
                (event.successCategories.size.toDouble() / totalCount * 100)
            } else {
                0.0
            }

        val emoji =
            when {
                successRate == 100.0 -> ":camera_with_flash:"
                successRate >= 50.0 -> ":warning:"
                else -> ":x:"
            }

        val statusMessage =
            when {
                successRate == 100.0 -> "Perfect! All categories uploaded to Instagram"
                successRate >= 50.0 -> "Partial success - some categories failed"
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
                                text = "$emoji *Instagram Upload Complete*",
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
                                        appendLine("• Success: *${event.successCategories.size}* / $totalCount categories")
                                        appendLine("• Success Rate: *${String.format("%.1f%%", successRate)}*")
                                        appendLine(
                                            "• Upload Time: ${event.uploadTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}",
                                        )
                                    },
                            ),
                    ),
                )

                if (event.successCategories.isNotEmpty()) {
                    add(
                        Block(
                            type = "section",
                            text =
                                Text(
                                    type = "mrkdwn",
                                    text = ":white_check_mark: *Success Categories:* ${event.successCategories.joinToString(
                                        ", ",
                                    ) { it.title }}",
                                ),
                        ),
                    )
                }

                if (event.failedCategories.isNotEmpty()) {
                    add(
                        Block(
                            type = "section",
                            text =
                                Text(
                                    type = "mrkdwn",
                                    text = ":x: *Failed Categories:* ${event.failedCategories.joinToString(", ") { it.title }}",
                                ),
                        ),
                    )
                }

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
                if (event.errorMessages.isNotEmpty()) {
                    add(Block(type = "divider"))
                    val errorDetails =
                        event.errorMessages.entries.joinToString("\n") { (category, message) ->
                            "[${category.title}] $message"
                        }
                    add(
                        Block(
                            type = "section",
                            text =
                                Text(
                                    type = "mrkdwn",
                                    text = ":warning: *Error Details:*\n```$errorDetails```",
                                ),
                        ),
                    )
                }
            }

        val slackBody = SlackBodyProperty(blocks = blocks)
        slackWebhookClient.sendAsync(slackBody)
    }
}