package com.few.generator.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeneratorDataSourceConfig {
    companion object {
        const val DATASOURCE = GeneratorConfig.BEAN_NAME_PREFIX + "DataSource"
    }

    @Bean(name = [DATASOURCE])
    @ConfigurationProperties(prefix = "spring.generator.datasource.hikari")
    fun dataSource(): HikariDataSource = HikariDataSource()
}