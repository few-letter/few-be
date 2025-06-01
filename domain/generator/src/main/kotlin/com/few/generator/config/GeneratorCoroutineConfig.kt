package com.few.generator.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeneratorCoroutineConfig {
    @Bean
    fun discordIoCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO.limitedParallelism(2))
}