package com.few.generator.config

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class GeneratorCoroutineConfig {
    private lateinit var coroutineScope: CoroutineScope

    @Bean
    fun discordIoCoroutineScope(): CoroutineScope {
        val executor =
            ThreadPoolTaskExecutor().apply {
                corePoolSize = 3
                maxPoolSize = 3
                queueCapacity = 10
                setThreadNamePrefix("discord-io-coroutine-")
                initialize()
            }
        coroutineScope = CoroutineScope(SupervisorJob() + executor.asCoroutineDispatcher())
        return coroutineScope
    }

    @PreDestroy
    fun cleanup() {
        if (::coroutineScope.isInitialized) {
            coroutineScope.cancel()
        }
    }
}