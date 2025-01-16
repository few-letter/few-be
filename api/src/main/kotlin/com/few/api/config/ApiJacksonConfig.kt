package com.few.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import event.domain.util.PublicationTargetIdentifierMixin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.modulith.events.core.PublicationTargetIdentifier

@Configuration
class ApiJacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.addMixIn(PublicationTargetIdentifier::class.java, PublicationTargetIdentifierMixin::class.java)
        mapper.registerModule(JavaTimeModule())
        return mapper
    }
}