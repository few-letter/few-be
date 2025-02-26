package com.few.generator.config.feign

import com.few.generator.core.gpt.prompt.schema.GptResponse

object ResponseClassThreadLocal {
    private val threadLocal = ThreadLocal<Class<out GptResponse>?>()

    fun set(responseClass: Class<out GptResponse>) {
        threadLocal.set(responseClass)
    }

    fun get(): Class<out GptResponse>? = threadLocal.get()

    fun clear() {
        threadLocal.remove()
    }
}