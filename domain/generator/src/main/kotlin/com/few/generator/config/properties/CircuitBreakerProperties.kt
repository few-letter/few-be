package com.few.generator.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@ConfigurationProperties("circuit-breaker")
data class CircuitBreakerProperties(
    var enabled: Boolean = true,
    var slidingWindowSize: Int = 10,
    var minimumNumberOfCalls: Int = 5,
    var failureRateThreshold: Float = 50f,
    var waitDurationInOpenState: Duration = Duration.ofSeconds(60),
)