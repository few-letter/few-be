package com.few.crm.config

import email.config.MailConfig
import event.config.EventConfig
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ComponentScan(basePackages = [CrmConfig.BASE_PACKAGE])
@Import(
    value = [
        MailConfig::class,
        EventConfig::class,
    ],
)
@ConfigurationPropertiesScan(basePackages = [CrmConfig.BASE_PACKAGE])
class CrmConfig {
    companion object {
        const val BASE_PACKAGE = "com.few.crm"
        const val BEAN_NAME_PREFIX = "fewCrm"
    }
}