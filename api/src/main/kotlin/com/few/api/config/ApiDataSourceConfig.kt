package com.few.api.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
class ApiDataSourceConfig {
    companion object {
        const val DATASOURCE = ApiConfig.BEAN_NAME_PREFIX + "DataSource"
    }

    @Primary
    @Bean(name = [DATASOURCE])
    @ConfigurationProperties(prefix = "spring.api.datasource.hikari")
    fun dataSource(): DataSource = DataSourceBuilder.create().type(HikariDataSource::class.java).build()
}