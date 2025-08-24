package com.few.email.config

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.few.email.config.properties.AwsEmailProviderProperties
import com.few.email.provider.AwsSendEmailServiceProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class AwsSendEmailServiceProviderConfig(
    private val amazonSimpleEmailService: AmazonSimpleEmailService,
    private val amazoneEmailProviderProperties: AwsEmailProviderProperties,
) {
    @Primary
    @Bean
    fun amazonSimpleEmailService(): AwsSendEmailServiceProvider =
        AwsSendEmailServiceProvider(
            amazonSimpleEmailService = amazonSimpleEmailService,
            configurationSetName = amazoneEmailProviderProperties.configurationSetName,
        )
}