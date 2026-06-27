package com.few.generator.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("alphavantage")
data class AlphaVantageProperties(
    var apiKey: String = "",
    var baseUrl: String = "https://www.alphavantage.co/query",
    var topFeedCount: Int = 4,
)