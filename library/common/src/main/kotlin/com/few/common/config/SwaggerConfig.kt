package com.few.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class SwaggerConfig(
    private val environment: Environment,
) {
    companion object {
        const val AUTH_TOKEN_KEY = "Authorization"
    }

    @Bean
    fun getOpenApi(): OpenAPI {
        val securityRequirement = SecurityRequirement().addList(AUTH_TOKEN_KEY)
        return OpenAPI()
            .components(authSetting())
            .security(listOf(securityRequirement))
            .addServersItem(Server().url("http://localhost:8080"))
            .addServersItem(Server().url("https://api.fewletter.store"))
            .info(
                Info()
                    .version("2.0.0")
                    .description("Few ${environment.activeProfiles[0].replaceFirstChar { it.uppercase() }} API Documentation")
                    .title("FEW API"),
            )
    }

    private fun authSetting(): Components =
        Components()
            .addSecuritySchemes(
                AUTH_TOKEN_KEY,
                SecurityScheme()
                    .description("Access Token")
                    .type(SecurityScheme.Type.APIKEY)
                    .`in`(SecurityScheme.In.HEADER)
                    .name(AUTH_TOKEN_KEY),
            )
}