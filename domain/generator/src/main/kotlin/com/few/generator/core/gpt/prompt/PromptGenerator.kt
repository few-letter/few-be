package com.few.generator.core.gpt.prompt

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.prompt.schema.*
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class PromptGenerator(
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
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

    fun toKoreanHighlightText(summary: String): Prompt {
        val systemPrompt =
            """
            당신은 월드 최고의 뉴스레터 기사 작성 전문가입니다. 원본 기사 제목, 요약, 중요한 문장들, AI로 생성된 헤드라인과 요약을 분석하여 하이라이트 텍스트를 추출합니다. 반드시 두 번 이상 검토하고 수정하여 제출해야 합니다.
            """.trimIndent()

        val userPrompt =
            """
            ## Instructions
            1. Input에 있는 [] 괄호 안에 들어있는 요약 내용 중에서 강조하고 싶은 하이라이트 텍스트를 추출해주세요.
            2. 하이라이트 텍스트는 한 문장으로 작성하되, 너무 길면(10자 이상) 문장의 일부를 발췌해서 추출해주세요.
            3. 본문에 있는 문장과 정확하게 일치해야 합니다.

            ## Input
            [$summary] 중에서 강조하고 싶은 하이라이트 텍스트를 1개 추출해주세요.
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemPrompt), Message(ROLE.USER, userPrompt)),
            responseFormat =
                ResponseFormat(
                    jsonSchema = JsonSchema(HighlightText.name, HighlightText.schema),
                    responseClassType = HighlightText::class.java,
                ),
        )
    }

    fun toKoreanKeyWords(coreTexts: String): Prompt {
        val systemPrompt =
            """
            당신은 월드 최고의 키워드 추출 전문가입니다. 주어진 텍스트에서 핵심 키워드를 추출합니다.
            """.trimIndent()

        val userPrompt =
            """
            ## Instructions
            1. 주어진 텍스트에서 5-10개의 핵심 키워드를 추출해주세요.
            2. 핵심 주제를 가장 잘 나타내는 명사와 용어를 선택하세요.
            3. 고유명사, 기술용어, 수치값을 우선적으로 포함하세요.
            4. 텍스트에 나타나는 형태 그대로 정확히 추출하세요.
            5. 일반적이거나 의미가 약한 단어는 제외하세요.
            6. 개별 단어나 매우 짧은 구문(최대 2-3단어)으로 추출하세요.
            7. 원본 텍스트의 정확한 철자, 대소문자, 형태를 보존하세요.

            ## Input
            텍스트: $coreTexts
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemPrompt), Message(ROLE.USER, userPrompt)),
            responseFormat =
                ResponseFormat(
                    jsonSchema = JsonSchema(Keywords.name, Keywords.schema),
                    responseClassType = Keywords::class.java,
                ),
        )
    }
}