package com.few.api.config

import com.few.api.config.ApiDataSourceConfig.Companion.DATASOURCE
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.TransactionManagementConfigurer
import javax.sql.DataSource

@Import(ApiConfig::class, ApiDataSourceConfig::class)
@Configuration
@EnableTransactionManagement
class ApiTxConfig(
    @Qualifier(DATASOURCE) private val dataSource: DataSource,
) : TransactionManagementConfigurer {
    companion object {
        const val DATASOURCE_TX = ApiConfig.BEAN_NAME_PREFIX + "DataSourceTransactionManager"
    }

    @Bean(name = [DATASOURCE_TX])
    fun dataSourceTransactionManager(): PlatformTransactionManager = DataSourceTransactionManager(dataSource)

    override fun annotationDrivenTransactionManager(): TransactionManager = dataSourceTransactionManager()
}