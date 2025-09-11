package com.few.api

import com.few.common.config.CommonConfig
import com.few.email.config.MailConfig
import com.few.generator.config.GeneratorConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(
    CommonConfig::class,
    GeneratorConfig::class,
    MailConfig::class,
)
@SpringBootApplication
class ApiMain

fun main(args: Array<String>) {
    runApplication<ApiMain>(*args)
}