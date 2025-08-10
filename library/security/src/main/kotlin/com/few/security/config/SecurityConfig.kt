package com.few.security.config

import com.few.security.encryptor.IdEncryptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = [SecurityConfig.BASE_PACKAGE])
class SecurityConfig {
    companion object {
        const val BASE_PACKAGE = " security"
        const val BEAN_NAME_PREFIX = "security"
        const val ID_ENCRYPTOR = BEAN_NAME_PREFIX + "IdEncryptor"
    }

    @Bean(name = [ID_ENCRYPTOR])
    fun idEncryptor(
        @Value("\${security.encryption.algorithm}")algorithm: String,
        @Value("\${security.encryption.secretKey}")secretKey: String,
        @Value("\${security.encryption.transformation}")transformation: String,
        @Value("\${security.encryption.keySize}")keySize: Int,
        @Value("\${security.encryption.iv}")iv: String,
    ): IdEncryptor = IdEncryptor(algorithm, secretKey, transformation, keySize, iv)
}