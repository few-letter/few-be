package com.few.provider.config

import com.few.provider.config.ProviderConfig.Companion.BEAN_NAME_PREFIX
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProviderSwaggerConfig {
    companion object {
        const val OPEN_API_BEAN_NAME = BEAN_NAME_PREFIX + "OpenApi"
    }

    @Bean(name = [OPEN_API_BEAN_NAME])
    fun providerApi(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("Provider API")
            .packagesToScan(ProviderConfig.BASE_PACKAGE)
            .build()
}