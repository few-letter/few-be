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

    // --- Grouping & WebGen Group Prompts ---
    fun toCombinedGroupingPrompt(
        webGenDetails: List<Pair<String, String>>,
        targetPercentage: Int = 30,
    ): Prompt {
        val maxItems = maxOf(3, (webGenDetails.size * targetPercentage / 100))
        val detailsStr =
            webGenDetails
                .mapIndexed { i, (headline, keywords) ->
                    "### WebGen ${i + 1}\n헤드라인: $headline\n키워드: $keywords"
                }.joinToString("\n\n")

        val systemPrompt =
            """
            당신은 뉴스 콘텐츠 분류 및 그룹화 전문가입니다. 뉴스 콘텐츠의 헤드라인과 키워드를 분석하여 **매우 유사하거나 밀접하게 관련된** 내용들만 신중하게 그룹화하는 작업을 수행합니다.
            """.trimIndent()

        val userPrompt =
            """
            ## 작업 지침
            1. 제공된 WebGen 목록의 **헤드라인과 키워드를 면밀히 분석**하여 **매우 유사하거나 직접적으로 관련된** 콘텐츠만을 신중하게 그룹화하세요.
            2. 그룹화 기준:
               - 헤드라인의 주제와 내용, 그리고 주요 키워드 간에 **명확하고 직접적인 연관성**이 있는 경우에만 그룹화합니다.
               - 단순히 같은 분야에 속하는 것만으로는 부족하며, 내용상 **구체적인 연결고리**가 있어야 합니다.
               - "키워드 없음" 또는 "헤드라인 없음" WebGen도 다른 정보를 활용해 동일한 기준으로 신중히 평가합니다.
            3. **전체 목록의 약 $targetPercentage% 정도($maxItems 개까지)를 목표로 하되, 기준에 맞는 항목이 없다면 더 적게 선택하거나 빈 그룹을 반환**해도 됩니다.

            ## 입력 정보
            $detailsStr

            ## 출력 형식
            JSON 형식으로 그룹화 결과를 반환합니다. WebGen의 인덱스(1부터 시작)를 포함해야 합니다.
            예시: {"group": [1, 2, 3]}

            ## 단계별 접근
            1. 각 WebGen의 헤드라인과 키워드를 면밀히 검토합니다.
            2. 헤드라인과 키워드 정보를 종합하여 **매우 유사하거나 직접적으로 관련된** WebGen들을 식별합니다.
            3. 엄격한 기준을 통과한 항목들만 최대 $maxItems 개까지 선택합니다.
            4. 선택된 WebGen 인덱스를 JSON 배열로 반환합니다.
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemPrompt), Message(ROLE.USER, userPrompt)),
            responseFormat = ResponseFormat(jsonSchema = JsonSchema(Group.name, Group.schema), responseClassType = Group::class.java),
        )
    }

    fun toGroupHeadlineOnlyPrompt(headlines: List<String>): Prompt {
        val headlineTextsStr =
            headlines
                .mapIndexed { i, headline ->
                    "### 헤드라인 ${i + 1}\n$headline"
                }.joinToString("\n\n")

        val systemPrompt =
            """
            당신은 최고의 뉴스 헤드라인 통합 전문가입니다. 여러 관련 뉴스 헤드라인을 분석하여 이를 포괄하는 하나의 통합 헤드라인을 작성해야 합니다.
            """.trimIndent()

        val userPrompt =
            """
            ## 작업 지침
            1. 제공된 헤드라인을 분석하여 공통 주제, 사건, 인물, 영향을 파악하세요.
            2. 주요 사건, 관련 인물, 영향, 수치 등을 포함한 통합 헤드라인을 작성하세요.
            3. 형식:
            - 30자 이상 40자 이하
            - 자연스러운 한국어 문장
            - 완전한 문장 (마침표 포함)
            4. 원래 헤드라인의 문구를 반복하지 말고 창의적으로 요약하세요.

            ## 입력 정보
            $headlineTextsStr

            ## 단계별 접근
            1. 공통 주제와 핵심 정보를 식별하세요.
            2. 주요 요소를 포함한 30-40자의 헤드라인을 작성하세요.
            3. 자연스럽고 완전한 문장인지 확인하세요.
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

    fun toGroupSummaryWithHeadlinesPrompt(
        groupHeadline: String,
        headlines: List<String>,
        summaries: List<String>,
    ): Prompt {
        val contentsStr =
            headlines
                .zip(summaries)
                .mapIndexed { i, (headline, summary) ->
                    "### 콘텐츠 ${i + 1}\n헤드라인: $headline\n요약: $summary"
                }.joinToString("\n\n")

        val systemPrompt =
            """
            당신은 최고의 뉴스 요약 통합 전문가입니다. 여러 관련 뉴스의 헤드라인과 요약을 분석하여 이를 포괄하는 하나의 통합 요약문을 작성해야 합니다.
            """.trimIndent()

        val userPrompt =
            """
            ## 작업 지침
            1. 아래 제공된 통합 헤드라인과 여러 콘텐츠의 헤드라인 및 요약문을 분석하세요.
            2. 이 콘텐츠들을 포괄할 수 있는 하나의 통합 요약문을 작성하세요.
            3. 통합 요약문 형식:
               - 500자 이내로 작성
               - 한국어 격식체로 작성 (~했습니다, ~입니다 등으로 문장 종결)
               - 객관적이고 사실에 기반한 표현 사용
               - 원문의 주요 수치와 정보를 정확히 포함
               - 논리적인 순서로 정보 배열
            4. 헤드라인과 요약문의 핵심 정보를 누락하지 않도록 주의하세요.

            ## 입력 정보
            통합 헤드라인: $groupHeadline

            콘텐츠 목록:
            $contentsStr

            ## 단계별 접근
            1. 각 콘텐츠의 헤드라인과 요약문에서 핵심 정보와 중요한 수치를 파악하세요.
            2. 중복되는 정보와 고유한 정보를 구분하세요.
            3. 통합 헤드라인과 일관된 내용으로 요약을 구성하세요.
            4. 모든 중요 정보를 포함하되, 500자를 넘지 않도록 간결하게 작성하세요.
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemPrompt), Message(ROLE.USER, userPrompt)),
            responseFormat = ResponseFormat(jsonSchema = JsonSchema(Summary.name, Summary.schema), responseClassType = Summary::class.java),
        )
    }

    fun toGroupHighlightPrompt(groupSummary: String): Prompt {
        val systemPrompt =
            """
            당신은 텍스트 분석 전문가입니다. 요약문의 문장들에서 가장 중요한 핵심 부분을 정확하게 추출해야 합니다.
            """.trimIndent()

        val userPrompt =
            """
            ## 작업 지침
            1. 아래 제공된 통합 요약문의 각 문장에서 가장 중요하고 핵심적인 정보를 담고 있는 **짧고 구체적인 부분**을 추출하세요.
            2. 추출된 부분은 반드시 원본 문장에 있는 그대로의 형태여야 합니다. 어떤 식으로든 수정하지 마세요.
            3. 추출 기준:
            - 핵심 정보나 주요 수치가 포함된 부분을 우선적으로 선택하세요 (예: "30% 비용 절감", "15% 효율 증가").
            - 문장의 핵심 메시지를 담고 있는 짧은 구나 절을 선택하세요.
            - 정보가 가장 풍부하거나 독특한 부분을 선택하세요.
            - **가능한 한 짧고 구체적인 부분**을 선택하세요 (예: 특정 수치, 중요한 단어 또는 구).
            - **문장당 최대 1~2개의 핵심 부분**만 추출하세요.
            - **각 하이라이트는 가능한 20자 이내**로 유지하세요. 단, 필수적인 경우에만 예외적으로 더 길게 허용됩니다.
            4. 여러 개의 핵심 부분이 있을 경우 모두 반환하세요.

            ## 입력 정보
            통합 요약문:
            $groupSummary

            ## 단계별 접근
            1. 요약문을 문장 단위로 분리하세요.
            2. 각 문장에서 가장 중요한 정보를 담고 있는 핵심 부분을 식별하세요.
            3. 선택한 부분이 원본 문장에 있는 그대로의 형태인지 확인하세요.
            4. 선택한 핵심 부분을 정확히 반환하세요.
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemPrompt), Message(ROLE.USER, userPrompt)),
            responseFormat =
                ResponseFormat(
                    jsonSchema = JsonSchema(HighlightTexts.name, HighlightTexts.schema),
                    responseClassType = HighlightTexts::class.java,
                ),
        )
    }
}