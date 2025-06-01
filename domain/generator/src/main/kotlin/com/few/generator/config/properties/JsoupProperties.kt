package com.few.generator.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jsoup")
data class JsoupProperties(
    val timeout: Int,
    val userAgent: String,
    val followRedirects: Boolean,
    val ignoreHttpErrors: Boolean,
    val ignoreContentType: Boolean,
)