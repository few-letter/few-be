package com.few.generator.core.gpt

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.prompt.Prompt
import com.few.generator.core.gpt.prompt.ROLE
import com.few.generator.core.gpt.prompt.schema.GptResponse
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.ResponseFormat
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class ChatGpt(
    private val openAiChatModel: OpenAiChatModel,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun ask(request: Prompt): GptResponse {
        log.info { "Open AI API Calling..." }

        val messages =
            request.messages.map { msg ->
                when (msg.role) {
                    ROLE.SYSTEM -> SystemMessage(msg.content)
                    ROLE.USER -> UserMessage(msg.content)
                }
            }

        val responseFormat =
            ResponseFormat
                .builder()
                .type(ResponseFormat.Type.JSON_SCHEMA)
                .jsonSchema(
                    ResponseFormat.JsonSchema
                        .builder()
                        .name(request.responseFormat.jsonSchema.name)
                        .schema(request.responseFormat.jsonSchema.schema)
                        .strict(true)
                        .build(),
                ).build()

        val options =
            OpenAiChatOptions
                .builder()
                .model(request.model.value)
                .temperature(request.temperature)
                .responseFormat(responseFormat)
                .build()

        val springAiPrompt =
            org.springframework.ai.chat.prompt
                .Prompt(messages, options)
        val chatResponse = openAiChatModel.call(springAiPrompt)

        val results = chatResponse.results
        if (results.isNullOrEmpty()) {
            throw RuntimeException("OpenAI API response has no choices")
        }

        log.info { "OpenAI API response choices count: ${results.size}" }

        val content =
            results[0].output.text
                ?: throw RuntimeException("OpenAI API response has no content")

        @Suppress("UNCHECKED_CAST")
        val result = gson.fromJson(content, request.responseFormat.responseClassType as Class<GptResponse>)
        result.completionId = chatResponse.metadata.id
        return result
    }
}