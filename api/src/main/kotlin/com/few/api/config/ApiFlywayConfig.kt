package com.few.api.config

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import java.util.function.Consumer
import javax.sql.DataSource

@Configuration
@Import(ApiDataSourceConfig::class)
class ApiFlywayConfig {
    companion object {
        const val FLYWAY = ApiConfig.BEAN_NAME_PREFIX + "Flyway"
        const val FLYWAY_VALIDATE_INITIALIZER = ApiConfig.BEAN_NAME_PREFIX + "FlywayValidateInitializer"
        const val FLYWAY_MIGRATION_INITIALIZER = ApiConfig.BEAN_NAME_PREFIX + "FlywayMigrationInitializer"
        const val FLYWAY_PROPERTIES = ApiConfig.BEAN_NAME_PREFIX + "FlywayProperties"
        const val FLYWAY_CONFIGURATION = ApiConfig.BEAN_NAME_PREFIX + "FlywayConfiguration"
    }

    @Bean(name = [FLYWAY])
    fun flyway(configuration: org.flywaydb.core.api.configuration.Configuration?): Flyway = Flyway(configuration)

    @Profile("!local") // TODO: Disable Flyway on local profile(Flyway삭제 예정)
    @Bean(name = [FLYWAY_VALIDATE_INITIALIZER])
    fun flywayValidateInitializer(flyway: Flyway?): FlywayMigrationInitializer =
        FlywayMigrationInitializer(flyway) { obj: Flyway -> obj.validate() }

    @Bean(name = [FLYWAY_MIGRATION_INITIALIZER])
    fun flywayMigrationInitializer(flyway: Flyway?): FlywayMigrationInitializer =
        FlywayMigrationInitializer(flyway) { obj: Flyway -> obj.migrate() }

    @Bean(name = [FLYWAY_PROPERTIES])
    @ConfigurationProperties(prefix = "spring.flyway.api")
    fun flywayProperties(): FlywayProperties = FlywayProperties()

    @Bean(name = [FLYWAY_CONFIGURATION])
    fun configuration(
        @Qualifier(ApiDataSourceConfig.DATASOURCE) dataSource: DataSource,
    ): org.flywaydb.core.api.configuration.Configuration {
        val configuration = FluentConfiguration()
        configuration.dataSource(dataSource)
        flywayProperties().locations.forEach(
            Consumer { locations: String? ->
                configuration.locations(
                    locations,
                )
            },
        )
        return configuration
    }
}