package web.config
import com.vaadin.flow.spring.annotation.EnableVaadin
import com.vaadin.hilla.crud.CrudConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Configuration

@Configuration
@EnableVaadin(
    value = [
        "com.few",
    ],
)
@EnableAutoConfiguration(
    exclude = [
        CrudConfiguration::class,
    ],
)
class VaadinConfig