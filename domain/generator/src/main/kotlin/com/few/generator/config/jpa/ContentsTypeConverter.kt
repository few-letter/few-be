package com.few.generator.config.jpa

import com.few.common.domain.ContentsType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class ContentsTypeConverter : AttributeConverter<ContentsType, Int> {
    override fun convertToDatabaseColumn(attribute: ContentsType?): Int? = attribute?.code

    override fun convertToEntityAttribute(dbData: Int?): ContentsType? = dbData?.let { ContentsType.fromCode(it) }
}