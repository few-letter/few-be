package com.few.generator.config.jpa

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.few.generator.domain.vo.GroupSourceHeadline
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kotlin.collections.emptyList

@Converter(autoApply = false)
class GroupSourceHeadlinesConverter : AttributeConverter<List<GroupSourceHeadline>, String> {
    private val objectMapper = jacksonObjectMapper()

    override fun convertToDatabaseColumn(attribute: List<GroupSourceHeadline>): String? =
        try {
            objectMapper.writeValueAsString(attribute)
        } catch (e: Exception) {
            "[]"
        }

    override fun convertToEntityAttribute(dbData: String?): List<GroupSourceHeadline>? =
        try {
            dbData?.takeIf { it.isNotBlank() }?.let {
                objectMapper.readValue(it, object : TypeReference<List<GroupSourceHeadline>>() {})
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
}