package com.few.generator.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class GeneratorAsyncConfig {
    @Bean(name = ["keywordExtractorExecutor"])
    fun keywordExtractorExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 4
        executor.maxPoolSize = 8
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("KeywordExtractor-")
        executor.initialize()
        return executor
    }

    @Bean(name = ["generatorSchedulingExecutor"])
    fun generatorSchedulingExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 5
        executor.queueCapacity = 3
        executor.setThreadNamePrefix("generatorScheduling-")
        executor.initialize()
        return executor
    }
}