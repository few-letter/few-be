package com.few.generator.config.feign

import com.few.generator.config.GeneratorGsonConfig
import com.few.generator.core.gpt.completion.ChatCompletion
import com.google.gson.Gson
import feign.Response
import feign.codec.ErrorDecoder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class OpenAiErrorDecoder(
    @Qualifier(GeneratorGsonConfig.GSON_BEAN_NAME)
    private val gson: Gson,
) : ErrorDecoder {
    private val log = KotlinLogging.logger {}
    private val defaultDecoder = ErrorDecoder.Default()

    override fun decode(
        methodKey: String,
        response: Response,
    ): Exception =
        runCatching {
            log.error { "Open AI API Status Code: ${response.status()}" }
            ResponseClassThreadLocal.clear()

            val responseBody =
                response
                    .body()
                    ?.asInputStream()
                    ?.reader()
                    ?.readText()
                    ?: throw RuntimeException("Empty response body from OpenAI API")

            val completion = gson.fromJson(responseBody, ChatCompletion::class.java)
            val refusal =
                completion.choices
                    ?.get(0)
                    ?.message
                    ?.refusal

            if (refusal != null) {
                throw RuntimeException("ChatGpt response choices count is 0. Refusal: ${refusal ?: "NotFound"}")
            }

            if (completion.error?.message != null) {
                throw RuntimeException("ChatGpt response error message: ${completion.error.message}")
            }

            defaultDecoder.decode(methodKey, response)
        }.getOrElse {
            log.error(it) { "Error while decoding response" }
            defaultDecoder.decode(methodKey, response)
        }.also {
            ResponseClassThreadLocal.clear()
        }
}