package com.few.generator.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("alphavantage")
data class AlphaVantageProperties(
    var apiKey: String = "",
)