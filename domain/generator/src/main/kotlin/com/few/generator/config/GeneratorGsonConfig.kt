package com.few.generator.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeneratorGsonConfig {
    companion object {
        const val GSON_BEAN_NAME = GeneratorConfig.BEAN_NAME_PREFIX + "Gson"
    }

    @Bean(GSON_BEAN_NAME)
    fun fewGson(): Gson =
        GsonBuilder()
            .setLenient()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()
}