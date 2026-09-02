package com.few.generator.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("scheduling")
data class SchedulingProperties(
    var localGen: Task = Task(),
    var globalGen: Task = Task(),
    var email: Task = Task(),
    var cacheMetrics: Task = Task(),
    var instagramTokenRefresh: Task = Task(),
    var stockBriefing: Task = Task(),
    var popularNasdaqStockNews: Task = Task(),
) {
    data class Task(
        var enabled: Boolean = false,
        var cron: String = "-",
    )
}