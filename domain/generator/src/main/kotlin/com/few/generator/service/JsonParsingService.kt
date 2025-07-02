package com.few.generator.service

import com.few.generator.core.constants.PromptConstants
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class JsonParsingService {
    private val log = KotlinLogging.logger {}

    fun parseHighlightTexts(rawTexts: String): List<String> {
        return try {
            if (rawTexts.isBlank()) {
                log.debug { "빈 하이라이트 텍스트, 빈 리스트 반환" }
                return emptyList()
            }

            if (rawTexts == PromptConstants.JsonParsing.EMPTY_JSON_ARRAY) {
                log.debug { "빈 JSON 배열, 빈 리스트 반환" }
                return emptyList()
            }

            val result =
                rawTexts
                    .removePrefix(PromptConstants.JsonParsing.ARRAY_START_BRACKET)
                    .removeSuffix(PromptConstants.JsonParsing.ARRAY_END_BRACKET)
                    .split(PromptConstants.JsonParsing.COMMA_SEPARATOR)
                    .map { it.trim().removeSurrounding(PromptConstants.JsonParsing.QUOTE_CHAR) }
                    .filter { it.isNotBlank() }

            log.debug { "하이라이트 텍스트 파싱 성공: ${result.size}개" }
            result
        } catch (e: Exception) {
            log.warn(e) { "하이라이트 텍스트 파싱 실패, 빈 리스트로 대체. 입력: $rawTexts" }
            emptyList()
        }
    }

    fun parseGroupIndices(groupIndices: String): List<Int> {
        return try {
            if (groupIndices.isBlank()) {
                log.debug { "빈 그룹 인덱스, 빈 리스트 반환" }
                return emptyList()
            }

            if (groupIndices == PromptConstants.JsonParsing.EMPTY_JSON_ARRAY) {
                log.debug { "빈 JSON 배열, 빈 리스트 반환" }
                return emptyList()
            }

            val result =
                groupIndices
                    .removePrefix(PromptConstants.JsonParsing.ARRAY_START_BRACKET)
                    .removeSuffix(PromptConstants.JsonParsing.ARRAY_END_BRACKET)
                    .split(PromptConstants.JsonParsing.COMMA_SEPARATOR)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .map { it.toInt() }

            log.debug { "그룹 인덱스 파싱 성공: ${result.size}개" }
            result
        } catch (e: Exception) {
            log.warn(e) { "그룹 인덱스 파싱 실패, 빈 리스트로 대체. 입력: $groupIndices" }
            emptyList()
        }
    }
}