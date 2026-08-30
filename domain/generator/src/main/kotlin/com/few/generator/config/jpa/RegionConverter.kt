package com.few.generator.config.jpa

import com.few.common.domain.Region
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class RegionConverter : AttributeConverter<Region, Int> {
    override fun convertToDatabaseColumn(attribute: Region?): Int? = attribute?.code

    override fun convertToEntityAttribute(dbData: Int?): Region? = dbData?.let { Region.from(it) }
}