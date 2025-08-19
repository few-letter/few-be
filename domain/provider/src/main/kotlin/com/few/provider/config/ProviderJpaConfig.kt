package com.few.provider.config

import jakarta.persistence.EntityManagerFactory
import org.hibernate.cfg.AvailableSettings
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.*
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.hibernate5.SpringBeanContainer
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.JpaVendorAdapter
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.util.ClassUtils
import javax.sql.DataSource

@Configuration
// @EnableJpaAuditing // TODO: generator 모듈에서와 중복됨(프로젝트 당 1개필요)
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = [ProviderConfig.BASE_PACKAGE],
    entityManagerFactoryRef = ProviderJpaConfig.ENTITY_MANAGER_FACTORY,
    transactionManagerRef = ProviderJpaConfig.TRANSACTION_MANAGER,
)
@Import(
    value = [
        ProviderDataSourceConfig::class,
    ],
)
@EnableAutoConfiguration(
    exclude = [
        DataSourceAutoConfiguration::class,
        DataSourceTransactionManagerAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
    ],
)
class ProviderJpaConfig {
    companion object {
        const val JPA_PROPERTIES = ProviderConfig.BEAN_NAME_PREFIX + "JpaProperties"
        const val HIBERNATE_PROPERTIES = ProviderConfig.BEAN_NAME_PREFIX + "HibernateProperties"
        const val JPA_VENDOR_ADAPTER = ProviderConfig.BEAN_NAME_PREFIX + "JpaVendorAdapter"
        const val ENTITY_MANAGER_FACTORY_BUILDER = ProviderConfig.BEAN_NAME_PREFIX + "EntityManagerFactoryBuilder"
        const val ENTITY_MANAGER_FACTORY = ProviderConfig.BEAN_NAME_PREFIX + "EntityManagerFactory"
        const val TRANSACTION_MANAGER = ProviderConfig.BEAN_NAME_PREFIX + "TransactionManager"
    }

    @Bean(name = [ENTITY_MANAGER_FACTORY])
    fun entityManagerFactory(
        @Qualifier(ProviderDataSourceConfig.DATASOURCE)
        dataSource: DataSource,
        @Qualifier(ENTITY_MANAGER_FACTORY_BUILDER)
        builder: EntityManagerFactoryBuilder,
        beanFactory: ConfigurableListableBeanFactory,
    ): LocalContainerEntityManagerFactoryBean {
        val jpaPropertyMap = jpaProperties().properties
        val settings = HibernateSettings()
        if (ClassUtils.isPresent("org.hibernate.resource.beans.container.spi.BeanContainer", javaClass.classLoader)) {
            val customizer =
                HibernatePropertiesCustomizer { properties: MutableMap<String?, Any?> ->
                    properties[AvailableSettings.BEAN_CONTAINER] = SpringBeanContainer(beanFactory)
                }
            settings.hibernatePropertiesCustomizers(listOf(customizer))
        }
        val hibernatePropertyMap =
            hibernateProperties().determineHibernateProperties(jpaPropertyMap, settings)

        return builder
            .dataSource(dataSource)
            .properties(hibernatePropertyMap)
            .packages(ProviderConfig.BASE_PACKAGE)
            .build()
    }

    @Bean(name = [TRANSACTION_MANAGER])
    fun transactionManager(
        @Qualifier(ENTITY_MANAGER_FACTORY)
        emf: EntityManagerFactory,
    ): PlatformTransactionManager = JpaTransactionManager(emf)

    @Bean(name = [JPA_PROPERTIES])
    @ConfigurationProperties("spring.provider.jpa")
    fun jpaProperties(): JpaProperties = JpaProperties()

    @Bean(name = [HIBERNATE_PROPERTIES])
    @ConfigurationProperties("spring.provider.jpa.hibernate")
    fun hibernateProperties(): HibernateProperties = HibernateProperties()

    @Bean(name = [JPA_VENDOR_ADAPTER])
    fun jpaVendorAdapter(): JpaVendorAdapter = HibernateJpaVendorAdapter()

    @Bean(name = [ENTITY_MANAGER_FACTORY_BUILDER])
    fun entityManagerFactoryBuilder(
        @Qualifier(JPA_VENDOR_ADAPTER)
        jpaVendorAdapter: JpaVendorAdapter,
        @Qualifier(JPA_PROPERTIES)
        jpaProperties: JpaProperties,
        persistenceUnitManager: ObjectProvider<PersistenceUnitManager>,
    ): EntityManagerFactoryBuilder {
        val jpaPropertyMap = jpaProperties.properties
        return EntityManagerFactoryBuilder(
            jpaVendorAdapter,
            jpaPropertyMap,
            persistenceUnitManager.getIfAvailable(),
        )
    }
}