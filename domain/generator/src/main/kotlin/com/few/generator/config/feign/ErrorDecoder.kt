package com.few.generator.config.feign

import feign.Response
import feign.codec.ErrorDecoder
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class ErrorDecoder : ErrorDecoder {
    private val log = KotlinLogging.logger {}
    private val defaultDecoder = ErrorDecoder.Default()

    override fun decode(
        methodKey: String,
        response: Response,
    ): Exception {
        log.error { "Open AI API Status Code: ${response.status()}" }
        ResponseClassThreadLocal.clear()
        return defaultDecoder.decode(methodKey, response)
    }
}