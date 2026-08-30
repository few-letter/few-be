package com.few.generator.config.jpa

import com.few.common.domain.MediaType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class MediaTypeConverter : AttributeConverter<MediaType, Int> {
    override fun convertToDatabaseColumn(attribute: MediaType?): Int? = attribute?.code

    override fun convertToEntityAttribute(dbData: Int?): MediaType? = dbData?.let { MediaType.from(it) }
}