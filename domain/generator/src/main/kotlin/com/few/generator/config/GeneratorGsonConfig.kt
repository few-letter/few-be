package com.few.generator.config

import com.google.gson.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
class GeneratorGsonConfig {
    companion object {
        const val GSON_BEAN_NAME = GeneratorConfig.BEAN_NAME_PREFIX + "Gson"
    }

    @Bean(GSON_BEAN_NAME)
    fun gson(): Gson =
        GsonBuilder()
            .setLenient()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
            .create()
}

class LocalDateTimeAdapter :
    JsonSerializer<LocalDateTime>,
    JsonDeserializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun serialize(
        src: LocalDateTime,
        typeOfSrc: Type,
        context: JsonSerializationContext,
    ): JsonElement = JsonPrimitive(src.format(formatter))

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): LocalDateTime = LocalDateTime.parse(json.asString, formatter)
}