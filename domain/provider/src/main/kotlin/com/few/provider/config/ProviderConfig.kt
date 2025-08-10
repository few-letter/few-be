package com.few.provider.config

import com.few.security.config.SecurityConfig
import com.few.web.config.WebConfig
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

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