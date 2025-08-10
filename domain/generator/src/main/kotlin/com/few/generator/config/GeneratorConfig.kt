package com.few.generator.config

import com.few.common.config.CommonConfig
import com.few.security.config.SecurityConfig
import com.few.web.config.WebConfig
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ComponentScan(basePackages = [GeneratorConfig.BASE_PACKAGE])
@Import(
    value = [
        CommonConfig::class,
        WebConfig::class,
        SecurityConfig::class,
    ],
)
class GeneratorConfig {
    companion object {
        const val BASE_PACKAGE = "com.few.generator"
        const val BEAN_NAME_PREFIX = "fewGenerator"
    }
}