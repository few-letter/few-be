package com.few.generator.config.feign

import feign.RequestInterceptor
import feign.RequestTemplate
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class OpenAiRequestInterceptor : RequestInterceptor {
    private val log = KotlinLogging.logger {}

    override fun apply(template: RequestTemplate?) {
        log.info { "Open AI API Calling..." }
    }
}