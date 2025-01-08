package com.few.crm.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsCredentials

@Configuration
class CrmAwsConfig {
    companion object {
        const val CRM_CREDENTIAL_PROVIDER = CrmConfig.BEAN_NAME_PREFIX + "CrmCredentialProvider"
    }

    @Value("\${crm.credentials.access-key}")
    val accessKey: String? = null

    @Value("\${crm.credentials.secret-key}")
    val secretKey: String? = null

    @Value("\${crm.region.static}")
    val region: String? = null

    @Bean(name = [CRM_CREDENTIAL_PROVIDER])
    fun credentialProvider(): AwsCredentials =
        object : AwsCredentials {
            override fun accessKeyId(): String = accessKey ?: ""

            override fun secretAccessKey(): String = secretKey ?: ""
        }
}