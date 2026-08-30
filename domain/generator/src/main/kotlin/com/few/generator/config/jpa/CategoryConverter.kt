package com.few.generator.config.jpa

import com.few.common.domain.Category
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class CategoryConverter : AttributeConverter<Category, Int> {
    override fun convertToDatabaseColumn(attribute: Category?): Int? = attribute?.code

    override fun convertToEntityAttribute(dbData: Int?): Category? = dbData?.let { Category.from(it) }
}