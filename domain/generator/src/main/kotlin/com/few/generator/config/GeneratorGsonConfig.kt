package com.few.generator.config

import com.few.generator.core.gpt.prompt.*
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
    fun gson(): Gson {
        val create =
            GsonBuilder()
                .registerTypeAdapter(ROLE::class.java, RoleSerializer())
                .registerTypeAdapter(ROLE::class.java, RoleDeserializer())
                .registerTypeAdapter(MODEL::class.java, ModelSerializer()) // 추가
                .registerTypeAdapter(MODEL::class.java, ModelDeserializer()) // 추가
                .setLenient()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
                .create()
        return create
    }
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

class ModelSerializer : JsonSerializer<MODEL> {
    override fun serialize(
        src: MODEL?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement = JsonPrimitive(src?.value)
}

class ModelDeserializer : JsonDeserializer<MODEL> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): MODEL =
        MODEL.fromValue(json?.asString ?: throw JsonParseException("Invalid MODEL value"))
            ?: throw JsonParseException("Unknown MODEL value: ${json?.asString}")
}

class RoleSerializer : JsonSerializer<ROLE> {
    override fun serialize(
        src: ROLE?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ): JsonElement = JsonPrimitive(src?.value)
}

class RoleDeserializer : JsonDeserializer<ROLE> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): ROLE =
        ROLE.fromValue(json?.asString ?: throw JsonParseException("Invalid ROLE value"))
            ?: throw JsonParseException("Unknown ROLE value: ${json?.asString}")
}