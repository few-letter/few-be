package com.few.crm.config

import com.few.crm.config.properties.CrmThreadPoolProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import web.config.ClonedTaskDecorator

@Configuration
class CrmThreadPoolConfig {
    private val log = KotlinLogging.logger {}

    companion object {
        const val CRM_LISTENER_POOL = "crm-listener-task-"
    }

    @Bean
    @ConfigurationProperties(prefix = "thread-pool.crm")
    fun crmThreadPoolProperties(): CrmThreadPoolProperties = CrmThreadPoolProperties()

    @Bean(CRM_LISTENER_POOL)
    fun crmListenerThreadPool() =
        ThreadPoolTaskExecutor().apply {
            val properties = crmThreadPoolProperties()
            corePoolSize = properties.getCorePoolSize()
            maxPoolSize = properties.getMaxPoolSize()
            queueCapacity = properties.getQueueCapacity()
            setWaitForTasksToCompleteOnShutdown(properties.getWaitForTasksToCompleteOnShutdown())
            setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds())
            setThreadNamePrefix("crmListenerThreadPool-")
            setRejectedExecutionHandler { r, _ ->
                log.warn { "Task $r has been rejected from crmListenerThreadPool" }
            }
            setTaskDecorator(ClonedTaskDecorator())
            initialize()
        }
}