package com.few.generator.core.gpt.prompt

import com.few.generator.core.gpt.prompt.schema.Texts
import org.springframework.stereotype.Component

@Component
class ProvisioningPromptGenerator {
    fun createBodyTexts(
        title: String,
        description: String,
        rawTexts: String,
    ): Prompt {
        val systemContent =
            """
            You are tasked with analyzing webpage metadata and content to extract relevant sentences. 
            You must extract sentences exactly as they appear, without any modifications.
            """.trimIndent()

        val userContent =
            """
            ## Instructions
            1. Extract all topic-related sentences from the given text exactly as they appear, without any modifications.
            2. Maintain the meaning of the text and include as many relevant sentences as possible without omissions.
            3. Do not reconstruct or edit the extracted sentences.

            ## Input
            1. Webpage Title: $title
            2. Webpage Content: $description
            3. Target Sentences to Extract: $rawTexts
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemContent), Message(ROLE.USER, userContent)),
            response_format = ResponseFormat(schema = Texts.schema, classType = Texts::class.java),
        )
    }

    fun createCoreTexts(
        title: String,
        description: String,
        bodyTexts: Texts,
    ): Prompt {
        val systemContent =
            """
            You are tasked with analyzing webpage title, summary, and content to extract important sentences. 
            You must extract sentences exactly as they appear, without any modifications.
            """.trimIndent()

        val userContent =
            """
            ## Instructions
            1. Extract all factual and content-wise important sentences from the given text exactly as they appear, without any modifications.
            2. Do not reconstruct or edit the extracted sentences.
            3. Include at least 50% of the target sentences.

            ## Input
            1. Webpage Title: $title
            2. Webpage Summary: $description
            3. Target Sentences to Extract: ${bodyTexts.texts.joinToString(", ")}
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemContent), Message(ROLE.USER, userContent)),
            response_format = ResponseFormat(schema = Texts.schema, classType = Texts::class.java),
        )
    }
}