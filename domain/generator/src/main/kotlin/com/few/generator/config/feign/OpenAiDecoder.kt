package com.few.generator.config.feign

import com.few.generator.config.GeneratorGsonConfig
import com.few.generator.core.gpt.completion.ChatCompletion
import com.few.generator.core.gpt.prompt.schema.GptResponse
import com.google.gson.Gson
import feign.Response
import feign.codec.Decoder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.lang.reflect.Type

@Component
class OpenAiDecoder(
    @Qualifier(GeneratorGsonConfig.GSON_BEAN_NAME)
    private val gson: Gson,
) : Decoder {
    private val log = KotlinLogging.logger {}

    override fun decode(
        response: Response,
        type: Type,
    ) = runCatching {
        val responseBody =
            response
                .body()
                ?.asInputStream()
                ?.reader()
                ?.readText()
                ?: throw RuntimeException("Empty response body")

        val completion = gson.fromJson(responseBody, ChatCompletion::class.java)
        validateResponse(completion)

        return@runCatching decodeFirstResponse(completion)
    }.onFailure {
        throw RuntimeException("Failed to decode response body", it)
    }.getOrThrow()
        .also {
            ResponseClassThreadLocal.clear()
        }

    private fun decodeFirstResponse(completion: ChatCompletion): GptResponse {
        val responseContentStr =
            completion.choices
                ?.find { it.index == 0 }
                ?.message
                ?.content ?: throw RuntimeException("No response found in ${completion.id} completion")

        val responseClass = ResponseClassThreadLocal.get() ?: throw RuntimeException("Response class not found in thread local")

        val responseDtoObj = gson.fromJson(responseContentStr, responseClass)
        responseDtoObj.completionId = completion.id

        return responseDtoObj
    }

    private fun validateResponse(completion: ChatCompletion) {
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