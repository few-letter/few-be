package com.few.generator.core.constants

object PromptConstants {
    object Grouping {
        const val DEFAULT_TARGET_PERCENTAGE = 30
        const val MIN_GROUP_SIZE = 3
    }

    object TextLimits {
        const val HEADLINE_MIN_LENGTH = 30
        const val HEADLINE_MAX_LENGTH = 40
        const val SUMMARY_MAX_LENGTH = 500
        const val HIGHLIGHT_MAX_LENGTH = 20
        const val IMPORTANT_SENTENCE_MIN_LENGTH = 10
    }

    object Roles {
        const val NEWSLETTER_EXPERT = "당신은 월드 최고의 뉴스레터 기사 작성 전문가입니다."
        const val NEWS_INTEGRATION_EXPERT = "당신은 최고의 뉴스 헤드라인 통합 전문가입니다."
        const val NEWS_SUMMARY_EXPERT = "당신은 최고의 뉴스 요약 통합 전문가입니다."
        const val NEWS_CLASSIFICATION_EXPERT = "당신은 뉴스 콘텐츠 분류 및 그룹화 전문가입니다."
        const val TEXT_ANALYSIS_EXPERT = "당신은 텍스트 분석 전문가입니다."
    }

    object PromptFormats {
        const val KOREAN_FORMAL_ENDING = "한국어 격식체로 작성 (~했습니다, ~입니다 등으로 문장 종결)"
        const val OBJECTIVE_EXPRESSION = "객관적이고 사실에 기반한 표현 사용"
        const val LOGICAL_ORDER = "논리적인 순서로 정보 배열"
        const val COMPLETE_SENTENCE_WITH_PERIOD = "완전한 문장 (마침표 포함)"
    }

    object JsonParsing {
        const val ARRAY_START_BRACKET = "["
        const val ARRAY_END_BRACKET = "]"
        const val COMMA_SEPARATOR = ","
        const val QUOTE_CHAR = "\""
        const val EMPTY_JSON_ARRAY = "[]"
    }
}