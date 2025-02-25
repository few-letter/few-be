package com.few.generator.config.feign

import com.few.generator.config.GeneratorGsonConfig
import com.few.generator.core.gpt.completion.ChatCompletion
import com.google.gson.Gson
import feign.Response
import feign.codec.Decoder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.InputStreamReader
import java.lang.reflect.Type

@Component
class GsonDecoder(
    @Qualifier(GeneratorGsonConfig.GSON_BEAN_NAME)
    private val gson: Gson,
) : Decoder {
    private val log = KotlinLogging.logger {}

    override fun decode(
        response: Response,
        type: Type,
    ): Any? {
        val body = response.body()
        if (body == null) {
            return null
        }
        val reader = InputStreamReader(body.asInputStream())
        val bodyObj = gson.fromJson(reader, ChatCompletion::class.java)

        postHandler(bodyObj)

        return bodyObj
    }

    private fun postHandler(completion: ChatCompletion) {
        val choicesCount = completion.choices?.size ?: 0
        val refusal =
            completion.choices
                ?.get(0)
                ?.message
                ?.refusal

        log.info { "Asking ChatGpt response choices count: $choicesCount" }

        if (choicesCount == 0 || refusal != null) {
            throw RuntimeException("ChatGpt response choices count is 0. Refusal: ${refusal ?: "NotFound"}")
        }
    }
}