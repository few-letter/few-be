package common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun getOpenApi(): OpenAPI =
        OpenAPI()
            .components(Components())
            .info(
                Info()
                    .version("2.0.0")
                    .description("Few API Documentation")
                    .title("FEW API"),
            )
}