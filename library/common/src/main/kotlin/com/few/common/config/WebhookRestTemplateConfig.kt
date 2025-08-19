package com.few.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class WebhookRestTemplateConfig {
    @Bean
    fun webhookRestTemplate(): RestTemplate {
        val requestFactory =
            SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(5_000)
                setReadTimeout(10_000)
            }

        return RestTemplate(requestFactory)
    }
}