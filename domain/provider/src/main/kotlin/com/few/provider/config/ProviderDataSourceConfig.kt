package com.few.provider.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProviderDataSourceConfig {
    companion object {
        const val DATASOURCE = ProviderConfig.BEAN_NAME_PREFIX + "DataSource"
        const val DATASOURCE_PROPERTIES = ProviderConfig.BEAN_NAME_PREFIX + "DataSourceProperties"
    }

    @Bean(name = [DATASOURCE])
    fun dataSource(
        @Qualifier(DATASOURCE_PROPERTIES) dataSourceProperties: DataSourceProperties,
    ): HikariDataSource {
        val hikariConfig =
            HikariConfig().apply {
                jdbcUrl = dataSourceProperties.url
                username = dataSourceProperties.username
                password = dataSourceProperties.password
                driverClassName = dataSourceProperties.driverClassName
                poolName = "Provider-POOL"
                maximumPoolSize = 16
                minimumIdle = 4
                connectionTimeout = 30000
                idleTimeout = 300000
                maxLifetime = 1800000
                connectionTestQuery = "SELECT 1"
            }
        return HikariDataSource(hikariConfig)
    }

    @Bean(name = [DATASOURCE_PROPERTIES])
    @ConfigurationProperties(prefix = "spring.provider.datasource")
    fun dataSourceProperties(): DataSourceProperties = DataSourceProperties()
}