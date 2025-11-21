package com.few.email

import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Component
class NewsletterEmailRenderer(
    private val emailHtmlTemplateEngine: TemplateEngine,
) : EmailRenderer<NewsletterModel> {
    override fun render(model: NewsletterModel): String {
        val context =
            Context().apply {
                setVariable("model", model)
            }
        return this.emailHtmlTemplateEngine.process("newsletter", context)
    }
}