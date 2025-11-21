package com.few.email.config

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.few.email.config.MailConfig.Companion.BEAN_NAME_PREFIX
import com.few.email.config.properties.AwsEmailProviderProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class MailSenderConfig {
    companion object {
        const val AWS_EMAIL_PROVIDER_PROPERTIES = BEAN_NAME_PREFIX + "AwsEmailProviderProperties"
        const val AWS_EMAIL_SENDER = BEAN_NAME_PREFIX + "AwsEmailProvider"
    }

    @Bean(name = [AWS_EMAIL_PROVIDER_PROPERTIES])
    @ConfigurationProperties(prefix = "few.email.provider.aws")
    fun awsProviderProperties(): AwsEmailProviderProperties = AwsEmailProviderProperties()

    @Bean(name = [AWS_EMAIL_SENDER])
    fun awsEmailSender(): AmazonSimpleEmailService {
        val properties = awsProviderProperties()

        return AmazonSimpleEmailServiceClientBuilder
            .standard()
            .withRegion(properties.region)
            .build()
    }
}