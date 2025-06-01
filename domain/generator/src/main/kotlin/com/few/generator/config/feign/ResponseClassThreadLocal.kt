package com.few.generator.config.feign

import com.few.generator.core.gpt.prompt.schema.GptResponse
import io.github.oshai.kotlinlogging.KotlinLogging

object ResponseClassThreadLocal {
    private val log = KotlinLogging.logger {}
    private val threadLocal = ThreadLocal<Class<out GptResponse>?>()

    fun set(responseClass: Class<out GptResponse>) {
        log.debug { "Setting thread local responseClass: $responseClass" }
        threadLocal.set(responseClass)
    }

    fun get(): Class<out GptResponse>? = threadLocal.get()

    fun clear() {
        log.debug { "Clearing thread local responseClass: ${get()}" }
        threadLocal.remove()
    }
}