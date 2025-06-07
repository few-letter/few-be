package com.few.generator.config.jpa

import com.few.generator.support.utils.CompressUtils
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class CompressedBase64Converter : AttributeConverter<String, String> {
    override fun convertToDatabaseColumn(attribute: String?): String? = attribute?.let { CompressUtils.compress(it) }

    override fun convertToEntityAttribute(dbData: String?): String? = dbData?.let { CompressUtils.decompress(it) }
}