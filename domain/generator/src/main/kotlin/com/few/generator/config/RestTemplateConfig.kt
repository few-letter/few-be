package com.few.generator.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {
    @Bean
    fun generatorRestTemplate(): RestTemplate {
        val factory =
            SimpleClientHttpRequestFactory().apply {
                // pooling 사용안함
                setConnectTimeout(120_000) // Connection Timeout: 2분
                setReadTimeout(120_000) // response Timeout: 2분
            }

        return RestTemplate(factory)
    }
}