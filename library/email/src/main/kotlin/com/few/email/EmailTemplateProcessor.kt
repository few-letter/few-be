package com.few.email

import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine

@Component
class EmailTemplateProcessor(
    private val templateEngines: Map<String, TemplateEngine>,
) {
    fun process(
        template: String,
        context: EmailContext,
        templateType: EmailTemplateType? = EmailTemplateType.HTML,
    ): String {
        templateEngines.keys
            .find {
                it.contains(templateType!!.name, ignoreCase = true)
            }?.let {
                return templateEngines[it]!!.process(template, context.getContext())
            } ?: throw IllegalArgumentException("Invalid template type")
    }
}