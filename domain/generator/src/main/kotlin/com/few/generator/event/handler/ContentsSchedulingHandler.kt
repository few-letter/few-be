package com.few.generator.event.handler

import com.few.generator.event.dto.ContentsSchedulingEventDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import web.client.DiscordBodyProperty
import web.client.Embed

@Component
class ContentsSchedulingHandler(
    private val discordRestTemplate: RestTemplate,
    @Value("\${urls.webhook.discord}") private val discordWebhookUrl: String,
) {
    private val log = KotlinLogging.logger {}

    suspend fun handle(event: ContentsSchedulingEventDto) {
        val body =
            DiscordBodyProperty(
                content = "🕐 콘텐츠 스케줄링 완료",
                embeds =
                    listOf(
                        Embed(
                            title = "✅ isSuccess",
                            description = event.isSuccess.toString(),
                        ),
                        Embed(
                            title = "⏰ 시작 시간",
                            description = event.startTime.toString(),
                        ),
                        Embed(
                            title = "1️⃣ RawContents 생성 시간",
                            description = event.timeOfCreatingRawContents,
                        ),
                        Embed(
                            title = "2️⃣ ProvisioningContents 생성 시간",
                            description = event.timeOfCreatingProvisionings,
                        ),
                        Embed(
                            title = "3️⃣ Gens 생성 시간",
                            description = event.timeOfCreatingGens,
                        ),
                        Embed(
                            title = "⌛ 전체 소요 시간",
                            description = event.totalTime,
                        ),
                        Embed(
                            title = ">> 카테고리 별 생성 컨텐츠 개수 <<",
                            description = event.countByCategory,
                        ),
                    ),
            )

        discordRestTemplate
            .exchange(
                discordWebhookUrl,
                HttpMethod.POST,
                HttpEntity(body),
                String::class.java,
            ).let { res ->
                log.info { "Discord webhook response: ${res.statusCode}" }
            }
    }
}