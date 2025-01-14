package com.few.crm.config.properties

import java.lang.IllegalStateException

data class CrmThreadPoolProperties(
    var corePoolSize: Int? = null,
    var maxPoolSize: Int? = null,
    var queueCapacity: Int? = null,
    var waitForTasksToCompleteOnShutdown: Boolean? = null,
    var awaitTerminationSeconds: Int? = null,
) {
    fun getCorePoolSize(): Int = corePoolSize ?: throw IllegalStateException("core pool size")

    fun getMaxPoolSize(): Int = maxPoolSize ?: throw IllegalStateException("max pool size")

    fun getQueueCapacity(): Int = queueCapacity ?: throw IllegalStateException("queue capacity")

    fun getWaitForTasksToCompleteOnShutdown(): Boolean =
        waitForTasksToCompleteOnShutdown ?: throw IllegalStateException("waitForTasksToCompleteOnShutdown")

    fun getAwaitTerminationSeconds(): Int = awaitTerminationSeconds ?: throw IllegalStateException("awaitTerminationSeconds")
}