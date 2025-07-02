package com.few.generator.core.gpt.prompt.generator

import com.few.generator.core.constants.PromptConstants
import com.few.generator.core.gpt.prompt.Prompt
import com.few.generator.core.gpt.prompt.base.BasePromptGenerator
import com.few.generator.core.gpt.prompt.schema.*
import org.springframework.stereotype.Component
import kotlin.math.max

@Component
class GroupPromptGenerator : BasePromptGenerator() {
    fun generateCombinedGroupingPrompt(
        webGenDetails: List<Pair<String, String>>,
        targetPercentage: Int = PromptConstants.Grouping.DEFAULT_TARGET_PERCENTAGE,
    ): Prompt {
        val maxItems = max(PromptConstants.Grouping.MIN_GROUP_SIZE, (webGenDetails.size * targetPercentage / 100))
        val detailsStr =
            webGenDetails
                .mapIndexed { i, (headline, keywords) ->
                    "### WebGen ${i + 1}\n헤드라인: $headline\n키워드: $keywords"
                }.joinToString("\n\n")

        val systemPrompt =
            buildSystemPrompt(
                expertRole = PromptConstants.Roles.NEWS_CLASSIFICATION_EXPERT,
                additionalContext = "뉴스 콘텐츠의 헤드라인과 키워드를 분석하여 **매우 유사하거나 밀접하게 관련된** 내용들만 신중하게 그룹화하는 작업을 수행합니다.",
            )

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

        return createPrompt<Group>(systemPrompt, userPrompt, Group.name, Group.schema)
    }

    fun generateGroupHeadlinePrompt(headlines: List<String>): Prompt {
        val headlineTextsStr =
            headlines
                .mapIndexed { i, headline ->
                    "### 헤드라인 ${i + 1}\n$headline"
                }.joinToString("\n\n")

        val systemPrompt =
            buildSystemPrompt(
                expertRole = PromptConstants.Roles.NEWS_INTEGRATION_EXPERT,
                additionalContext = "여러 관련 뉴스 헤드라인을 분석하여 이를 포괄하는 하나의 통합 헤드라인을 작성해야 합니다.",
            )

        val userPrompt =
            """
            ## 작업 지침
            1. 제공된 헤드라인을 분석하여 공통 주제, 사건, 인물, 영향을 파악하세요.
            2. 주요 사건, 관련 인물, 영향, 수치 등을 포함한 통합 헤드라인을 작성하세요.
            3. 형식:
            - ${PromptConstants.TextLimits.HEADLINE_MIN_LENGTH}자 이상 ${PromptConstants.TextLimits.HEADLINE_MAX_LENGTH}자 이하
            - 자연스러운 한국어 문장
            - ${PromptConstants.PromptFormats.COMPLETE_SENTENCE_WITH_PERIOD}
            4. 원래 헤드라인의 문구를 반복하지 말고 창의적으로 요약하세요.

            ## 입력 정보
            $headlineTextsStr

            ## 단계별 접근
            1. 공통 주제와 핵심 정보를 식별하세요.
            2. 주요 요소를 포함한 ${PromptConstants.TextLimits.HEADLINE_MIN_LENGTH}-${PromptConstants.TextLimits.HEADLINE_MAX_LENGTH}자의 헤드라인을 작성하세요.
            3. 자연스럽고 완전한 문장인지 확인하세요.
            """.trimIndent()

        return createPrompt<Headline>(systemPrompt, userPrompt, Headline.name, Headline.schema)
    }

    fun generateGroupSummaryPrompt(
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
            buildSystemPrompt(
                expertRole = PromptConstants.Roles.NEWS_SUMMARY_EXPERT,
                additionalContext = "여러 관련 뉴스의 헤드라인과 요약을 분석하여 이를 포괄하는 하나의 통합 요약문을 작성해야 합니다.",
            )

        val userPrompt =
            """
            ## 작업 지침
            1. 아래 제공된 통합 헤드라인과 여러 콘텐츠의 헤드라인 및 요약문을 분석하세요.
            2. 이 콘텐츠들을 포괄할 수 있는 하나의 통합 요약문을 작성하세요.
            3. 통합 요약문 형식:
               - ${PromptConstants.TextLimits.SUMMARY_MAX_LENGTH}자 이내로 작성
               - ${PromptConstants.PromptFormats.KOREAN_FORMAL_ENDING}
               - ${PromptConstants.PromptFormats.OBJECTIVE_EXPRESSION}
               - 원문의 주요 수치와 정보를 정확히 포함
               - ${PromptConstants.PromptFormats.LOGICAL_ORDER}
            4. 헤드라인과 요약문의 핵심 정보를 누락하지 않도록 주의하세요.

            ## 입력 정보
            통합 헤드라인: $groupHeadline

            콘텐츠 목록:
            $contentsStr

            ## 단계별 접근
            1. 각 콘텐츠의 헤드라인과 요약문에서 핵심 정보와 중요한 수치를 파악하세요.
            2. 중복되는 정보와 고유한 정보를 구분하세요.
            3. 통합 헤드라인과 일관된 내용으로 요약을 구성하세요.
            4. 모든 중요 정보를 포함하되, ${PromptConstants.TextLimits.SUMMARY_MAX_LENGTH}자를 넘지 않도록 간결하게 작성하세요.
            """.trimIndent()

        return createPrompt<Summary>(systemPrompt, userPrompt, Summary.name, Summary.schema)
    }

    fun generateGroupHighlightPrompt(groupSummary: String): Prompt {
        val systemPrompt =
            buildSystemPrompt(
                expertRole = PromptConstants.Roles.TEXT_ANALYSIS_EXPERT,
                additionalContext = "요약문의 문장들에서 가장 중요한 핵심 부분을 정확하게 추출해야 합니다.",
            )

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
            - **각 하이라이트는 가능한 ${PromptConstants.TextLimits.HIGHLIGHT_MAX_LENGTH}자 이내**로 유지하세요. 단, 필수적인 경우에만 예외적으로 더 길게 허용됩니다.
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

        return createPrompt<HighlightTexts>(systemPrompt, userPrompt, HighlightTexts.name, HighlightTexts.schema)
    }
}