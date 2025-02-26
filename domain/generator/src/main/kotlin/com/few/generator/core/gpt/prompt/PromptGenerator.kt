package com.few.generator.core.gpt.prompt

import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.core.gpt.prompt.schema.Texts
import org.springframework.stereotype.Component

@Component
class PromptGenerator {
    fun toHeadline(
        title: String,
        description: String,
        coreTexts: String,
    ): Prompt {
        val systemContent =
            """
            You are a world-renowned newsletter article writing expert tasked with analyzing webpage title, summary, and content to extract headlines. Always review and revise your response at least twice before submitting.
            """.trimIndent()

        val userContent =
            """
            ## Instructions
            1. Reflect specific and important content from the input, naturally incorporating relevant keywords and core concepts into the title.
            2. The title should not be a mere summary but should be specific and in-depth enough for readers to grasp the core content just by reading the title.

            ## Input
            1. Webpage Title: $title
            2. Webpage Summary: $description
            3. Webpage Content: $coreTexts
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemContent), Message(ROLE.USER, userContent)),
            response_format = ResponseFormat(schema = Headline.schema, responseClassType = Headline::class.java),
        )
    }

    fun toSummary(
        title: String,
        description: String,
        coreTexts: String,
    ): Prompt {
        val systemContent =
            """
            You are tasked with analyzing webpage title, summary, and content to create a summary. Always review and revise your response at least twice before submitting.
            """.trimIndent()

        val userContent =
            """
            ## Instructions
            1. Reflect specific and important content from the input, naturally incorporating relevant keywords and core concepts into the summary.
            2. Write the summary in natural sentence form, using only basic punctuation marks like commas and periods, avoiding special characters like exclamation marks and question marks.

            ## Input
            1. Webpage Title: $title
            2. Webpage Summary: $description
            3. Webpage Content: $coreTexts
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemContent), Message(ROLE.USER, userContent)),
            response_format = ResponseFormat(schema = Summary.schema, responseClassType = Summary::class.java),
        )
    }

    fun toBodyTexts(
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
            response_format = ResponseFormat(schema = Texts.schema, responseClassType = Texts::class.java),
        )
    }

    fun toCoreTexts(
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
            response_format = ResponseFormat(schema = Texts.schema, responseClassType = Texts::class.java),
        )
    }
}