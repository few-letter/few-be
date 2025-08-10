package com.few.provider.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import security.config.SecurityConfig
import web.config.WebConfig

@Configuration
@ComponentScan(basePackages = [ProviderConfig.BASE_PACKAGE])
@Import(
    value = [
        WebConfig::class,
        SecurityConfig::class,
    ],
)
class ProviderConfig {
    companion object {
        const val BASE_PACKAGE = "com.few.provider"
        const val BEAN_NAME_PREFIX = "fewProvider"
    }
}