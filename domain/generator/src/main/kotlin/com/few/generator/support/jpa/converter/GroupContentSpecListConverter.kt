package com.few.generator.support.jpa.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.few.generator.core.model.GroupContentSpec
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.stereotype.Component

@Component
@Converter
class GroupContentSpecListConverter(
    private val objectMapper: ObjectMapper,
) : AttributeConverter<List<GroupContentSpec>, String> {
    override fun convertToDatabaseColumn(attribute: List<GroupContentSpec>): String = objectMapper.writeValueAsString(attribute)

    override fun convertToEntityAttribute(dbData: String): List<GroupContentSpec> =
        objectMapper.readValue(dbData, objectMapper.typeFactory.constructCollectionType(List::class.java, GroupContentSpec::class.java))
}