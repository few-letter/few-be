package com.few.generator.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "generator.grouping")
data class GroupingProperties(
    var targetPercentage: Int = 30,
    var minGroupSize: Int = 3,
    var maxGroupSize: Int = 10,
    var similarityThreshold: Double = 0.7,
)