package com.few.generator.core.gpt.prompt

import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.core.gpt.prompt.schema.Texts
import org.springframework.stereotype.Component

@Component
class PromptGenerator {
    fun toHeadlineKorean(
        title: String,
        description: String,
        headline: String,
        summary: String,
    ): Prompt {
        val systemContent =
            """
            당신은 월드 최고의 뉴스레터 기사 작성 전문가입니다. 웹페이지 제목, 요약, 내용을 분석하여 헤드라인을 추출합니다. 반드시 두 번 이상 검토하고 수정하여 제출해야 합니다. 제목은 35자 이내로 작성해주세요.
            """.trimIndent()

        val userContent =
            """
            ## Instructions
            1. 35자 이내로 작성해주세요.
            2. 헤드라인을 자연스러운 한국어 문장으로 작성해주세요.
            3. 구체적이고 수치적으로 중요한 내용은 포함하고, 헤드라인만 읽어도 내용이 충분히 이해가 될 수 있도록 작성해주세요.

            ## Input
            1. 원본 기사 제목: $title
            2. 원본 기사 요약: $description
            3. AI로 생성된 헤드라인: $headline
            4. AI로 생성된 요약: $summary
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemContent), Message(ROLE.USER, userContent)),
            response_format = ResponseFormat(schema = Headline.schema, responseClassType = Headline::class.java),
        )
    }

    fun toHeadlineDefault(
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

    fun toSummaryKorean(
        title: String,
        description: String,
        coreTexts: String,
        headline: String,
        summary: String,
    ): Prompt {
        val systemContent =
            """
            당신은 월드 최고의 뉴스레터 기사 작성 전문가입니다. 원본 기사 제목, 요약, 중요한 문장들, AI로 생성된 헤드라인과 요약을 분석하여 요약을 작성합니다. 2개의 문단으로 작성하고 각 문단은 70자 이내의 문장으로 작성해주고 문단은 줄내림으로 구분해주세요. 문장은 자연스러운 한국어 격식체로 작성해주세요. (~했습니다, ~입니다 등으로 끝맺고 구어체를 배제하며 자연스럽게 표현) 반드시 두 번 이상 검토하고 수정하여 제출해야 합니다. 
            """.trimIndent()

        val userContent =
            """
            ## Instructions
            1. 두개의 문단으로 작성해주세요. 각 문단은 70자이내의 문장으로 작성해주고 문단은 줄내림으로 구분해주세요.
            2. 구체적이고 수치적으로 중요한 내용은 포함하고, 요약만 읽어도 내용이 충분히 이해가 될 수 있도록 작성해주세요.
            3. 통계적이고 객관적이고 수치적으로 올바른 문장들을 중요한 문장들에 근거하여 간결하게 작성해주세요.

            ## Input
            1. 원본 기사 제목: $title
            2. 원본 기사 요약: $description
            3. AI로 생성된 헤드라인: $headline
            4. AI로 생성된 요약: $summary
            5. (참고용) 중요한 문장들: $coreTexts
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemContent), Message(ROLE.USER, userContent)),
            response_format = ResponseFormat(schema = Summary.schema, responseClassType = Summary::class.java),
        )
    }

    fun toSummaryDefault(
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