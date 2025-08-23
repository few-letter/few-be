package com.few.generator.config.scrapper

import com.few.generator.config.properties.JsoupProperties
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Deprecated("Use ScrapperOkHttpFactory instead")
@Configuration
@EnableConfigurationProperties(JsoupProperties::class)
class JsoupConfig(
    private val properties: JsoupProperties,
) {
    @Bean
    fun jsoupConnectionFactory() = JsoupConnectionFactory(properties)
}

class JsoupConnectionFactory(
    private val properties: JsoupProperties,
) {
    fun createConnection(url: String): Connection =
        Jsoup
            .connect(url)
            .timeout(properties.timeout)
            .userAgent(properties.userAgent)
            .followRedirects(properties.followRedirects)
            .ignoreHttpErrors(properties.ignoreHttpErrors)
            .ignoreContentType(properties.ignoreContentType)
}