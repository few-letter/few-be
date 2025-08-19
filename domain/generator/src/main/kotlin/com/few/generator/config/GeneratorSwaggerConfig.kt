package com.few.generator.config

import com.few.generator.config.GeneratorConfig.Companion.BEAN_NAME_PREFIX
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeneratorSwaggerConfig {
    companion object {
        const val OPEN_API_BEAN_NAME = BEAN_NAME_PREFIX + "OpenApi"
    }

    @Bean(name = [OPEN_API_BEAN_NAME])
    fun generatorApi(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("Generator API")
            .packagesToScan(GeneratorConfig.BASE_PACKAGE)
            .build()
}