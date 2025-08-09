package com.few.provider.support.jpa

import com.few.provider.config.ProviderJpaConfig
import org.springframework.core.annotation.AliasFor
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * Api DataSource 트랜잭션을 처리하는 어노테이션
 * transactionManager는 DataSourceConfig에서 설정한 값을 기본으로 사용한다.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Transactional(transactionManager = ProviderJpaConfig.TRANSACTION_MANAGER)
annotation class ProviderTransactional(
    @get:AliasFor(annotation = Transactional::class, attribute = "propagation")
    val propagation: Propagation = Propagation.REQUIRED,
    @get:AliasFor(annotation = Transactional::class, attribute = "isolation")
    val isolation: Isolation = Isolation.DEFAULT,
    @get:AliasFor(annotation = Transactional::class, attribute = "readOnly")
    val readOnly: Boolean = false,
)