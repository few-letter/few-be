package com.few.generator.config

import common.config.CommonConfig
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import security.config.SecurityConfig
import web.config.WebConfig

@Configuration
@ComponentScan(basePackages = [GeneratorConfig.BASE_PACKAGE])
@Import(
    value = [
        WebConfig::class,
        SecurityConfig::class,
        CommonConfig::class,
    ],
)
class GeneratorConfig {
    companion object {
        const val BASE_PACKAGE = "com.few.generator"
        const val BEAN_NAME_PREFIX = "fewGenerator"
    }
}