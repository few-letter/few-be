package com.few.common.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = [CommonConfig.BASE_PACKAGE])
class CommonConfig {
    companion object {
        const val BASE_PACKAGE = "common"
        const val BEAN_NAME_PREFIX = "common"
    }
}