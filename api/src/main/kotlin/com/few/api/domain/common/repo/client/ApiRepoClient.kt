package com.few.api.domain.common.repo.client

import com.few.api.domain.common.repo.client.dto.RepoAlterArgs
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import web.client.DiscordBodyProperty
import web.client.Embed

@Service
class ApiRepoClient(
    private val apiDiscordRestTemplate: RestTemplate,
    @Value("\${webhook.discord}") private val discordWebhook: String,
) {
    private val log = KotlinLogging.logger {}

    fun announceRepoAlter(args: RepoAlterArgs) {
        val embedsList =
            mutableListOf(
                Embed(
                    title = "Exception",
                    description = "Slow Query Detected",
                ),
            )

        args.let { arg ->
            arg.requestURL.let { requestURL ->
                embedsList.add(
                    Embed(
                        title = "Request URL",
                        description = requestURL,
                    ),
                )
            }

            arg.query?.let { query ->
                embedsList.add(
                    Embed(
                        title = "Slow Query Detected",
                        description = query,
                    ),
                )
            }
        }

        apiDiscordRestTemplate
            .exchange(
                discordWebhook,
                HttpMethod.POST,
                HttpEntity(
                    DiscordBodyProperty(
                        content = "😭 DB 이상 발생",
                        embeds = embedsList,
                    ),
                ),
                String::class.java,
            ).let { res ->
                log.info { "Discord webhook response: ${res.statusCode}" }
            }
    }
}