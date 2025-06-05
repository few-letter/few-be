package com.few.generator.config

import feign.Retryer
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableFeignClients(
    basePackages = [
        GeneratorConfig.BASE_PACKAGE,
    ],
)
class GeneratorFeignConfig {
    @Bean
    fun feignRetryer(): Retryer =
        Retryer.Default(
            TimeUnit.SECONDS.toMillis(1),
            TimeUnit.SECONDS.toMillis(1),
            1,
        )
}