package com.few.generator.core.gpt.prompt.generator

import com.few.generator.core.constants.PromptConstants
import com.few.generator.core.gpt.prompt.Prompt
import com.few.generator.core.gpt.prompt.base.BasePromptGenerator
import com.few.generator.core.gpt.prompt.schema.*
import com.few.generator.domain.Category
import org.springframework.stereotype.Component

@Component
class ContentPromptGenerator : BasePromptGenerator() {
    fun generateHeadlinePrompt(
        title: String,
        description: String,
        category: Category,
        rawTexts: List<String>,
    ): Prompt {
        val systemPrompt =
            buildSystemPrompt(
                expertRole = PromptConstants.Roles.NEWSLETTER_EXPERT,
                additionalContext = "원본 기사 제목, 요약, 중요한 문장들, AI로 생성된 헤드라인과 요약을 분석하여 뉴스레터용 한글 헤드라인을 작성합니다.",
            )

        val rawTextsJson = rawTexts.joinToString(", ") { "\"$it\"" }
        val userPrompt =
            """
            ## Instructions
            1. 원본 기사를 분석하여 ${PromptConstants.TextLimits.HEADLINE_MIN_LENGTH}자 이상 ${PromptConstants.TextLimits.HEADLINE_MAX_LENGTH}자 이하의 뉴스레터용 한글 헤드라인을 작성해주세요.
            2. 헤드라인은 기사의 핵심 내용을 정확히 전달하면서도 독자의 관심을 끌 수 있어야 합니다.
            3. ${PromptConstants.PromptFormats.COMPLETE_SENTENCE_WITH_PERIOD}
            4. 카테고리: ${category.title}에 맞는 톤앤매너를 사용해주세요.

            ## 입력 정보
            1. 원본 제목: $title
            2. 원본 요약: $description
            3. 중요한 문장들: [$rawTextsJson]
            """.trimIndent()

        return createPrompt<Headline>(systemPrompt, userPrompt, Headline.name, Headline.schema)
    }

    fun generateSummaryPrompt(
        title: String,
        description: String,
        category: Category,
        rawTexts: List<String>,
        coreTexts: List<String>,
    ): Prompt {
        val systemPrompt =
            buildSystemPrompt(
                expertRole = PromptConstants.Roles.NEWSLETTER_EXPERT,
                additionalContext = "원본 기사 제목, 요약, 중요한 문장들을 분석하여 뉴스레터용 한글 요약문을 작성합니다.",
            )

        val rawTextsJson = rawTexts.joinToString(", ") { "\"$it\"" }
        val coreTextsJson = coreTexts.joinToString(", ") { "\"$it\"" }

        val userPrompt =
            """
            ## Instructions
            1. 원본 기사를 분석하여 ${PromptConstants.TextLimits.SUMMARY_MAX_LENGTH}자 이내의 뉴스레터용 한글 요약문을 작성해주세요.
            2. ${PromptConstants.PromptFormats.KOREAN_FORMAL_ENDING}
            3. ${PromptConstants.PromptFormats.OBJECTIVE_EXPRESSION}
            4. 원문의 주요 수치와 정보를 정확히 포함해주세요.
            5. ${PromptConstants.PromptFormats.LOGICAL_ORDER}
            6. 카테고리: ${category.title}에 맞는 톤앤매너를 사용해주세요.

            ## 입력 정보
            1. 원본 제목: $title
            2. 원본 요약: $description
            3. (참고용) 중요한 문장들: [$rawTextsJson]
            4. (참고용) 핵심 문장들: [$coreTextsJson]
            """.trimIndent()

        return createPrompt<Summary>(systemPrompt, userPrompt, Summary.name, Summary.schema)
    }

    fun generateHighlightTextPrompt(summary: String): Prompt {
        val systemPrompt =
            buildSystemPrompt(
                expertRole = PromptConstants.Roles.NEWSLETTER_EXPERT,
                additionalContext = "요약 내용 중에서 강조하고 싶은 하이라이트 텍스트를 추출합니다.",
            )

        val userPrompt =
            """
            ## Instructions
            1. Input에 있는 [] 괄호 안에 들어있는 요약 내용 중에서 강조하고 싶은 하이라이트 텍스트를 추출해주세요.
            2. 하이라이트 텍스트는 한 문장으로 작성하되, 너무 길면(${PromptConstants.TextLimits.IMPORTANT_SENTENCE_MIN_LENGTH}자 이상) 문장의 일부를 발췌해서 추출해주세요.
            3. 본문에 있는 문장과 정확하게 일치해야 합니다.

            ## Input
            [$summary] 중에서 강조하고 싶은 하이라이트 텍스트를 1개 추출해주세요.
            """.trimIndent()

        return createPrompt<HighlightText>(systemPrompt, userPrompt, HighlightText.name, HighlightText.schema)
    }
}