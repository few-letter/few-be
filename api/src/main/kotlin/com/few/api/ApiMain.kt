package com.few.api

import com.few.common.config.CommonConfig
import com.few.generator.config.GeneratorConfig
import com.few.provider.config.ProviderConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    CommonConfig::class,
    GeneratorConfig::class,
    ProviderConfig::class,
)
@SpringBootApplication
class ApiMain

fun main(args: Array<String>) {
    runApplication<ApiMain>(*args)
}