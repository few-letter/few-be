package com.few.generator.config

import feign.RequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeneratorOpenAiFeignConfiguration(
    @Value("\${openai.api.key}") private val apiKey: String,
) {
    companion object {
        const val REQUEST_INTERCEPTOR_BEAN_NAME = GeneratorConfig.BEAN_NAME_PREFIX + "OpenAiRequestInterceptor"
    }

    @Bean(REQUEST_INTERCEPTOR_BEAN_NAME)
    fun requestInterceptor(): RequestInterceptor =
        RequestInterceptor { template ->
            template.header("Authorization", "Bearer $apiKey")
            template.header("Content-Type", "application/json")
        }
}