package com.few.generator.config

import jakarta.persistence.EntityManagerFactory
import org.hibernate.cfg.AvailableSettings
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
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

/**
 * [org.springframework.data.jpa.repository.config.EnableJpaAuditing] is configured at [com.few.crm.config.CrmJpaConfig]
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = [GeneratorConfig.BASE_PACKAGE],
    entityManagerFactoryRef = GeneratorJpaConfig.ENTITY_MANAGER_FACTORY,
    transactionManagerRef = GeneratorJpaConfig.TRANSACTION_MANAGER,
)
@Import(
    value = [
        GeneratorDataSourceConfig::class,
    ],
)
class GeneratorJpaConfig {
    companion object {
        const val JPA_PROPERTIES = GeneratorConfig.BEAN_NAME_PREFIX + "JpaProperties"
        const val HIBERNATE_PROPERTIES = GeneratorConfig.BEAN_NAME_PREFIX + "HibernateProperties"
        const val JPA_VENDOR_ADAPTER = GeneratorConfig.BEAN_NAME_PREFIX + "JpaVendorAdapter"
        const val ENTITY_MANAGER_FACTORY_BUILDER = GeneratorConfig.BEAN_NAME_PREFIX + "EntityManagerFactoryBuilder"
        const val ENTITY_MANAGER_FACTORY = GeneratorConfig.BEAN_NAME_PREFIX + "EntityManagerFactory"
        const val TRANSACTION_MANAGER = GeneratorConfig.BEAN_NAME_PREFIX + "TransactionManager"
    }

    @Bean(name = [ENTITY_MANAGER_FACTORY])
    fun entityManagerFactory(
        @Qualifier(GeneratorDataSourceConfig.DATASOURCE)
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
            .packages(GeneratorConfig.BASE_PACKAGE)
            .build()
    }

    @Bean(name = [TRANSACTION_MANAGER])
    fun transactionManager(
        @Qualifier(ENTITY_MANAGER_FACTORY)
        emf: EntityManagerFactory,
    ): PlatformTransactionManager = JpaTransactionManager(emf)

    @Bean(name = [JPA_PROPERTIES])
    @ConfigurationProperties("spring.generator.jpa")
    fun jpaProperties(): JpaProperties = JpaProperties()

    @Bean(name = [HIBERNATE_PROPERTIES])
    @ConfigurationProperties("spring.generator.jpa.hibernate")
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