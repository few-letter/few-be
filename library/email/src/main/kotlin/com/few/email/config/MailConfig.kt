package com.few.email.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = [MailConfig.BASE_PACKAGE])
class MailConfig {
    companion object {
        const val BASE_PACKAGE = "com.few.email"
        const val BEAN_NAME_PREFIX = "email"
    }
}