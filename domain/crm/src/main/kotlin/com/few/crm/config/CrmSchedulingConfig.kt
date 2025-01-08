package com.few.crm.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.core.retry.RetryMode
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.scheduler.SchedulerClient
import java.time.Duration

@Configuration
@EnableScheduling
@Import(CrmAwsConfig::class)
class CrmSchedulingConfig {
    companion object {
        const val CRM_SCHEDULER_CLIENT = CrmConfig.BEAN_NAME_PREFIX + "CrmSchedulerClient"
    }

    @Bean(name = [CRM_SCHEDULER_CLIENT])
    fun awsSchedulerClient(
        @Qualifier(CrmAwsConfig.CRM_CREDENTIAL_PROVIDER) awsCredentials: AwsCredentials,
    ): SchedulerClient {
        val overrideConfig =
            ClientOverrideConfiguration
                .builder()
                .apiCallTimeout(Duration.ofMinutes(2))
                .apiCallAttemptTimeout(Duration.ofSeconds(90))
                .retryStrategy(RetryMode.STANDARD)
                .build()

        return SchedulerClient
            .builder()
            .region(Region.AP_NORTHEAST_2)
            .overrideConfiguration(overrideConfig)
            .credentialsProvider(
                AwsCredentialsProvider { awsCredentials },
            ).build()
    }
}