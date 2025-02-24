package com.few.generator.core.gpt

import com.few.generator.client.GeneratorOpenAiClient
import com.few.generator.core.gpt.prompt.Prompt
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class ChatGpt(
    private val openAiClient: GeneratorOpenAiClient,
) {
    private val log = KotlinLogging.logger {}

    fun ask(prompt: Prompt): String {
        val response = openAiClient.send(prompt)

        val choicesCount = response.choices?.size ?: 0
        log.info { "Asking ChatGpt response choices count: $choicesCount" }

        return response.choices
            ?.find { it.index == 0 }
            ?.message
            ?.content ?: throw RuntimeException("No response found in ${response.id} asking")
    }
}