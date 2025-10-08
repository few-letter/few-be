package com.few.generator.core.gpt.prompt

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.prompt.schema.*
import com.few.generator.domain.vo.GenDetail
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
        rawTexts: String,
    ): Prompt {
        val systemPrompt =
            """
            당신은 웹페이지 메타데이터와 본문을 분석하여 관련 문장을 정확히 추출하는 전문가입니다.
            모든 문장은 원문 그대로 추출해야 합니다. 수정이나 요약을 금지합니다.
            """.trimIndent()

        val userPrompt =
            """
            ## 지침
            1. 주어진 텍스트에서 주제와 관련된 모든 문장을 수정 없이 있는 그대로 추출하세요.
            2. 텍스트의 의미를 유지하고 누락 없이 가능한 한 많은 관련 문장을 포함하세요.
            3. 원문을 재구성하지 말고 그대로 인용하시오.
            4. 본문은 너무 길 경우 일부 생략될 수 있습니다. 핵심 문장만 사용하세요.

            ## 입력
            1. 웹페이지 제목: $title
            2. 원문 문장들: $rawTexts
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
        bodyTexts: Texts,
    ): Prompt {
        val systemPrompt =
            """
            당신은 웹페이지 메타데이터와 본문을 분석하여 관련 문장을 정확히 추출하는 전문가입니다.
            모든 문장은 원문 그대로 추출해야 합니다. 수정이나 요약을 금지합니다.
            """.trimIndent()

        val userPrompt =
            """
            ## 지침
            1. 주어진 텍스트에서 사실적이고 내용적으로 중요한 모든 문장을 수정 없이 있는 그대로 추출하십시오.
            2. 원문을 재구성하지 말고 그대로 인용하시오.
            3. 대상 문장의 최소 50%를 포함하십시오.
            4. 본문은 너무 길 경우 일부 생략될 수 있습니다. 핵심 문장만 사용하세요.

            ## 입력
            1. 웹페이지 제목: $title
            2. 원문 문장들 (JSON Array): ${gson.toJson(bodyTexts.texts)}
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
            4. 문장형 종결어미(~입니다, ~했다 등)를 사용하지 말고, '명사로 끝나는 구문'으로 작성하세요.(예: '애플의 신제품 발표', '삼성 주가 급등' 등)

            ## Input
            1. 원본 기사 제목: $title
            2. 원본 기사 내용: $coreTextsJson
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
            2. (참고용) 중요한 문장들: $coreTextsJson
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
            1. 아래 Input의 대괄호([]) 안 텍스트에서만 하이라이트를 추출하세요.
            2. 하이라이트는 한 문장 또는 핵심 키워드/구문으로 추출하세요. 길 경우(대략 13자 이상) 원문 일부만 발췌하세요.
            3. 반드시 아래 Input의 원문과 철자·띄어쓰기·숫자·기호까지 100% 일치해야 합니다.
            4. 강조: 출력은 아래 Input에서 그대로 복사한 문자열이어야 합니다. 문장 전체가 아니어도 되며, 단어 또는 일부 구문이어도 됩니다. 단, 원문을 재구성하지 말고 그대로 인용하시오.

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

    fun toCombinedGroupingPrompt(
        genDetails: List<GenDetail>,
        targetPercentage: Int = 30,
    ): Prompt {
        val systemPrompt =
            """
            당신은 월드 최고의 뉴스 그룹화 전문가입니다. 주어진 헤드라인과 키워드를 분석하여 유사한 주제의 뉴스들을 그룹화합니다.
            """.trimIndent()

        val genList =
            genDetails
                .mapIndexed { index, genDetail ->
                    "${index + 1}. 헤드라인: \"${genDetail.headline}\", 키워드: \"${genDetail.keywords}\""
                }.joinToString("\n")

        val targetCount = (genDetails.size * targetPercentage / 100).coerceAtLeast(1)

        val userPrompt =
            """
            ## Instructions
            1. 주어진 뉴스들 중에서 유사한 주제나 관련성이 높은 뉴스들을 그룹화하세요.
            2. 그룹에 포함될 뉴스의 번호를 배열로 반환하세요.
            3. 목표: 전체 ${genDetails.size}개 중 약 $targetPercentage%인 ${targetCount}개 정도를 하나의 그룹으로 선택하세요.
            4. 헤드라인과 키워드를 모두 고려하여 가장 관련성이 높은 뉴스들을 선택하세요.
            5. 만약 충분히 유사한 뉴스가 없다면 빈 배열을 반환하세요.

            ## 뉴스 목록
            $genList

            ## 출력 형식
            선택된 뉴스 번호들을 배열로 반환하세요.
            """.trimIndent()

        return Prompt(
            messages = listOf(Message(ROLE.SYSTEM, systemPrompt), Message(ROLE.USER, userPrompt)),
            responseFormat =
                ResponseFormat(
                    jsonSchema = JsonSchema(Group.name, Group.schema),
                    responseClassType = Group::class.java,
                ),
        )
    }

    fun toGroupHeadlineOnlyPrompt(headlines: List<String>): Prompt {
        val systemPrompt =
            """
            당신은 월드 최고의 뉴스 헤드라인 작성 전문가입니다. 여러 관련 뉴스의 헤드라인을 종합하여 하나의 통합 헤드라인을 작성합니다.
            """.trimIndent()

        val headlineList = headlines.joinToString("\n") { "- $it" }

        val userPrompt =
            """
            ## Instructions
            1. 주어진 헤드라인들의 공통 주제를 파악하세요.
            2. 모든 헤드라인의 핵심 내용을 포괄하는 통합 헤드라인을 작성하세요.
            3. 간결하고 명확하며 흥미를 끄는 헤드라인으로 작성하세요.
            4. 한국어로 작성하고, 30자 이내로 제한하세요.
            5. 문장형 종결어미(~입니다, ~했다 등)를 사용하지 말고, '명사로 끝나는 구문'으로 작성하세요.(예: '애플의 신제품 발표', '삼성 주가 급등' 등)

            ## 헤드라인 목록
            $headlineList
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
        val systemPrompt =
            """
            당신은 월드 최고의 뉴스 요약 전문가입니다. 여러 관련 뉴스의 내용을 종합하여 하나의 통합 요약을 작성합니다.
            """.trimIndent()

        val contentList =
            headlines
                .zip(summaries) { headline, summary ->
                    "제목: $headline\n내용: $summary"
                }.joinToString("\n\n")

        val userPrompt =
            """
            ## Instructions
            1. 주어진 여러 뉴스의 내용을 종합하여 하나의 통합 요약을 작성하세요.
            2. 그룹 헤드라인: "$groupHeadline"에 맞는 내용으로 작성하세요.
            3. 중복되는 내용은 제거하고 핵심 정보만 포함하세요.
            4. 논리적 순서로 구성하고 완성된 문단 형태로 작성하세요.
            5. 100-150자 내외로 작성하세요.

            ## 뉴스 내용
            $contentList
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

    fun toGroupHighlightPrompt(groupSummary: String): Prompt {
        val systemPrompt =
            """
            당신은 월드 최고의 뉴스레터 기사 작성 전문가입니다. 그룹 요약에서 하이라이트 텍스트들을 추출합니다.
            """.trimIndent()

        val userPrompt =
            """
            ## Instructions
            1. 주어진 그룹 요약에서 강조하고 싶은 하이라이트 텍스트들을 추출해주세요.
            2. 각 하이라이트는 한 문장 또는 핵심 구문으로 작성하세요.
            3. 2-4개의 하이라이트를 추출하세요.
            4. 강조: 출력은 아래 Input에서 그대로 복사한 문자열이어야 합니다. 문장 전체가 아니어도 되며, 단어 또는 일부 구문이어도 됩니다. 단, 원문을 재구성하지 말고 그대로 인용하시오.

            ## 그룹 요약(Input)
            $groupSummary
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