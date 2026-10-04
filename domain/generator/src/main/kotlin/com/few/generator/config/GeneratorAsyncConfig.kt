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
        executor.corePoolSize = 1
        executor.maxPoolSize = 1
        executor.queueCapacity = 1
        executor.setThreadNamePrefix("KeywordExtractor-")
        executor.setTaskDecorator(MdcTaskDecorator())
        executor.initialize()
        return executor
    }

    @Bean(name = ["generatorSchedulingExecutor"])
    fun generatorSchedulingExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 3
        executor.maxPoolSize = 3
        executor.queueCapacity = 3
        executor.setThreadNamePrefix("gen-sched-")
        executor.setTaskDecorator(MdcTaskDecorator())
        executor.initialize()
        return executor
    }
}