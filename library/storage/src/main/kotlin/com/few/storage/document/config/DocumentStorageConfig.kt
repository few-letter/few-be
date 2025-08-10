package com.few.storage.document.config

import com.few.storage.config.StorageClientConfig
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@ComponentScan(basePackages = [DocumentStorageConfig.BASE_PACKAGE])
@Import(StorageClientConfig::class)
class DocumentStorageConfig {
    companion object {
        const val BASE_PACKAGE = "storage.document"
        const val BEAN_NAME_PREFIX = "documentStore"
    }
}