package com.few.generator.core.gpt.prompt

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.prompt.generator.ContentPromptGenerator
import com.few.generator.core.gpt.prompt.generator.GroupPromptGenerator
import com.few.generator.core.gpt.prompt.schema.*
import com.few.generator.domain.Category
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class PromptGenerator(
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
    private val contentPromptGenerator: ContentPromptGenerator,
    private val groupPromptGenerator: GroupPromptGenerator,
) {
    fun toBodyTexts(
        title: String,
        description: String,
        rawTexts: String,
    ): Prompt {
        val systemPrompt =
            """
            You are tasked with analyzing webpage metadata and content to extract relevant sentences. 
            You must extract sentences exactly as they appear, without any modifications.
            """.trimIndent()

        val userPrompt =
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
            messages = listOf(Message(ROLE.SYSTEM, systemPrompt), Message(ROLE.USER, userPrompt)),
            responseFormat =
                ResponseFormat(
                    jsonSchema = JsonSchema(Texts.name, Texts.schema),
                    responseClassType = Texts::class.java,
                ),
        )
    }

    fun toCoreTexts(
        title: String,
        description: String,
        bodyTexts: Texts,
    ): Prompt {
        val systemPrompt =
            """
            You are tasked with analyzing webpage title, summary, and content to extract important sentences. 
            You must extract sentences exactly as they appear, without any modifications.
            """.trimIndent()

        val userPrompt =
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
            messages = listOf(Message(ROLE.SYSTEM, systemPrompt), Message(ROLE.USER, userPrompt)),
            responseFormat =
                ResponseFormat(
                    jsonSchema = JsonSchema(Texts.name, Texts.schema),
                    responseClassType = Texts::class.java,
                ),
        )
    }

    fun toHeadlineShort(
        title: String,
        description: String,
        coreTextsJson: String,
    ): Prompt {
        val systemPrompt =
            """
            당신은 월드 최고의 뉴스레터 기사 작성 전문가입니다. 한 줄짜리 요약의 카드뉴스를 만들려고 하는데, 구체적인 수치나, 한문장만 읽어도 전체 핵심 내용이 이해되게 작성해야합니다. 반드시 두 번 이상 검토하고 명확한 근거를 가지고 수정하여 제출해야 합니다.
            """.trimIndent()

        val userPrompt =
            """
            ## Instructions
            1. 20자 이상 30자 이내로 작성해주세요.
            2. 자연스러운 한국어 문장으로 단답식으로 작성해주세요. 느낌표, 물음표 사용없이, 마침표만 사용해주세요.
            3. 핵심 수치나, 구체적인 기업명, 시간이나 날짜, 장소명 등등의 내용이 모두 포함되게 작성해주세요.

            ## Input
            1. 원본 기사 제목: $title
            2. 원본 기사 요약: $description
            3. 원본 기사 내용: $coreTextsJson
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemPrompt), Message(ROLE.USER, userPrompt)),
            responseFormat =
                ResponseFormat(
                    jsonSchema = JsonSchema(Headline.name, Headline.schema),
                    responseClassType = Headline::class.java,
                ),
        )
    }

    fun toSummaryShort(
        headline: String,
        title: String,
        description: String,
        coreTextsJson: String,
    ): Prompt {
        val systemPrompt =
            """
            당신은 월드 최고의 뉴스레터 기사 작성 전문가입니다. [제목]에 맞는 본문을 작성합니다. 
            """.trimIndent()

        val userPrompt =
            """
            ## Instructions
            1. 반드시 230자 이내로 작성해주세요.
            2. 문장은 자연스러운 한국어 격식체로 작성해주세요. (~했습니다, ~입니다 등으로 끝맺고 구어체를 배제하며 자연스럽게 표현)
            3. 통계적이고 객관적이고 수치적으로 올바른 문장들을 중요한 문장들에 근거하여 간결하게 작성해주세요.

            ## Input
            0. [제목]: $headline
            1. 원본 기사 제목: $title
            2. 원본 기사 요약: $description
            3. (참고용) 중요한 문장들: $coreTextsJson
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemPrompt), Message(ROLE.USER, userPrompt)),
            responseFormat =
                ResponseFormat(
                    jsonSchema = JsonSchema(Summary.name, Summary.schema),
                    responseClassType = Summary::class.java,
                ),
        )
    }

    fun toKoreanHeadline(
        title: String,
        description: String,
        category: Category,
        rawTexts: List<String>,
    ): Prompt = contentPromptGenerator.generateHeadlinePrompt(title, description, category, rawTexts)

    fun toKoreanSummary(
        title: String,
        description: String,
        category: Category,
        rawTexts: List<String>,
        coreTexts: List<String>,
    ): Prompt = contentPromptGenerator.generateSummaryPrompt(title, description, category, rawTexts, coreTexts)

    fun toKoreanHighlightText(summary: String): Prompt = contentPromptGenerator.generateHighlightTextPrompt(summary)

    fun toCombinedGroupingPrompt(
        genDetails: List<Pair<String, String>>, // headline and keyWords
        targetPercentage: Int = 30,
    ): Prompt = groupPromptGenerator.generateCombinedGroupingPrompt(genDetails, targetPercentage)

    fun toGroupHeadlineOnlyPrompt(headlines: List<String>): Prompt = groupPromptGenerator.generateGroupHeadlinePrompt(headlines)

    fun toGroupSummaryWithHeadlinesPrompt(
        groupHeadline: String,
        headlines: List<String>,
        summaries: List<String>,
    ): Prompt = groupPromptGenerator.generateGroupSummaryPrompt(groupHeadline, headlines, summaries)

    fun toGroupHighlightPrompt(groupSummary: String): Prompt = groupPromptGenerator.generateGroupHighlightPrompt(groupSummary)

    fun toKoreanKeyWords(coreTextsJson: String): Prompt = contentPromptGenerator.generateKeyWordsPrompt(coreTextsJson)
}