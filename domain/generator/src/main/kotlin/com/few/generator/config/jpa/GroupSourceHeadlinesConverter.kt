package com.few.generator.config.jpa

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.few.generator.domain.vo.GroupSourceHeadline
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kotlin.collections.emptyList

@Converter(autoApply = false)
class GroupSourceHeadlinesConverter : AttributeConverter<List<GroupSourceHeadline>, String> {
    private val objectMapper = jacksonObjectMapper()
    private val log = KotlinLogging.logger {}

    companion object {
        private const val EMPTY_JSON_ARRAY = "[]"
    }

    override fun convertToDatabaseColumn(attribute: List<GroupSourceHeadline>): String? =
        try {
            if (attribute.isEmpty()) {
                EMPTY_JSON_ARRAY
            } else {
                val jsonResult = objectMapper.writeValueAsString(attribute)
                log.debug { "GroupSourceHeadlines 직렬화 성공: ${attribute.size}개 아이템" }
                jsonResult
            }
        } catch (e: Exception) {
            log.warn(e) { "GroupSourceHeadlines 직렬화 실패, 빈 배열로 대체. 입력: $attribute" }
            EMPTY_JSON_ARRAY
        }

    override fun convertToEntityAttribute(dbData: String?): List<GroupSourceHeadline>? =
        try {
            when {
                dbData.isNullOrBlank() -> {
                    log.debug { "DB 데이터가 비어있음, 빈 리스트 반환" }
                    emptyList()
                }
                dbData == EMPTY_JSON_ARRAY -> {
                    log.debug { "DB 데이터가 빈 JSON 배열, 빈 리스트 반환" }
                    emptyList()
                }
                else -> {
                    val result = objectMapper.readValue(dbData, object : TypeReference<List<GroupSourceHeadline>>() {})
                    log.debug { "GroupSourceHeadlines 역직렬화 성공: ${result.size}개 아이템" }
                    result
                }
            }
        } catch (e: Exception) {
            log.warn(e) { "GroupSourceHeadlines 역직렬화 실패, 빈 리스트로 대체. DB 데이터: $dbData" }
            emptyList()
        }
}