package com.few.generator.config

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@Configuration
@EnableFeignClients(
    basePackages = [
        GeneratorConfig.BASE_PACKAGE,
    ],
)
class GeneratorFeignConfig