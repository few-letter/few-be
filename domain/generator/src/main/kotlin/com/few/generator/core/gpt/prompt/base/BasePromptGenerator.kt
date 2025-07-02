package com.few.generator.core.gpt.prompt.base

import com.few.generator.core.gpt.prompt.Message
import com.few.generator.core.gpt.prompt.Prompt
import com.few.generator.core.gpt.prompt.ROLE
import com.few.generator.core.gpt.prompt.schema.GptResponse
import com.few.generator.core.gpt.util.ResponseFormatFactory

abstract class BasePromptGenerator {
    protected inline fun <reified T : GptResponse> createPrompt(
        systemPrompt: String,
        userPrompt: String,
        schemaName: String,
        schema: Map<String, Any>,
    ): Prompt =
        Prompt(
            messages =
                listOf(
                    Message(ROLE.SYSTEM, systemPrompt),
                    Message(ROLE.USER, userPrompt),
                ),
            responseFormat = ResponseFormatFactory.create<T>(schemaName, schema),
        )

    protected fun buildSystemPrompt(
        expertRole: String,
        additionalContext: String = "",
    ): String =
        if (additionalContext.isNotEmpty()) {
            "$expertRole $additionalContext"
        } else {
            expertRole
        }
}