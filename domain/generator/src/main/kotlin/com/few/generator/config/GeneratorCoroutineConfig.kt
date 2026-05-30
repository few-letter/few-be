package com.few.generator.config

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeneratorCoroutineConfig {
    @Bean(name = ["instagramCoroutineScope"])
    fun instagramCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("instagram-coroutine"))

    @Bean(name = ["groupGenCoroutineScope"])
    fun groupGenCoroutineScope(): CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("group-gen-coroutine"))
}