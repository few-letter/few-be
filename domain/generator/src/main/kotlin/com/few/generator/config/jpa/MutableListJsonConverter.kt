package com.few.generator.config.jpa

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.beans.factory.annotation.Qualifier

@Converter(autoApply = false)
class MutableListJsonConverter(
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) : AttributeConverter<MutableList<String>, String> {
    override fun convertToDatabaseColumn(attribute: MutableList<String>?): String =
        gson.toJson(attribute?.filterNotNull()?.takeIf { it.isNotEmpty() } ?: mutableListOf<String>())

    override fun convertToEntityAttribute(dbData: String?): MutableList<String> =
        dbData?.let {
            gson.fromJson(it, object : TypeToken<MutableList<String>>() {}.type)
        } ?: mutableListOf()
}