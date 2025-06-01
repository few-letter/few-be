package email.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.StringTemplateResolver

@Configuration
@EnableAutoConfiguration(exclude = [ThymeleafAutoConfiguration::class])
class ThymeleafConfig {
    companion object {
        const val HTML_TEMPLATE_ENGINE = MailConfig.BEAN_NAME_PREFIX + "HtmlTemplateEngine"
        const val STRING_TEMPLATE_ENGINE = MailConfig.BEAN_NAME_PREFIX + "StringTemplateEngine"
        const val SPRING_RESOURCE_TEMPLATE_RESOLVER = MailConfig.BEAN_NAME_PREFIX + "SpringResourceTemplateResolver"
    }

    @Bean(name = [HTML_TEMPLATE_ENGINE])
    fun htmlTemplateEngine(): SpringTemplateEngine {
        val templateEngine = SpringTemplateEngine()
        templateEngine.addTemplateResolver(springResourceTemplateResolver())
        return templateEngine
    }

    @Bean(name = [SPRING_RESOURCE_TEMPLATE_RESOLVER])
    fun springResourceTemplateResolver(): SpringResourceTemplateResolver {
        val springResourceTemplateResolver = SpringResourceTemplateResolver()
        springResourceTemplateResolver.order = 1
        springResourceTemplateResolver.prefix = "classpath:templates/"
        springResourceTemplateResolver.suffix = ".html"
        springResourceTemplateResolver.setTemplateMode(TemplateMode.HTML)
        springResourceTemplateResolver.characterEncoding = "UTF-8"
        springResourceTemplateResolver.isCacheable = false
        return springResourceTemplateResolver
    }

    @Bean(name = [STRING_TEMPLATE_ENGINE])
    fun stringTemplateEngine(): SpringTemplateEngine {
        val templateEngine = SpringTemplateEngine()
        templateEngine.addTemplateResolver(stringTemplateResolver())
        return templateEngine
    }

    @Bean
    fun stringTemplateResolver(): StringTemplateResolver {
        val stringTemplateResolver = StringTemplateResolver()
        stringTemplateResolver.setTemplateMode("HTML")
        stringTemplateResolver.isCacheable = false
        return stringTemplateResolver
    }
}