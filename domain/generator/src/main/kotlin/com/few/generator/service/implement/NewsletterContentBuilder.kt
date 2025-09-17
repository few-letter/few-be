package com.few.generator.service.implement

import com.few.email.EmailContext
import com.few.email.GenData
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class NewsletterContentBuilder {
    fun buildEmailContext(
        date: LocalDate,
        gensByCategory: Map<Int, List<GenData>>,
    ): EmailContext {
        val context = EmailContext()

        val templateData = NewsletterTemplateData.from(date, gensByCategory)

        context.setVariable("date", templateData.date)
        context.setVariable("gensByCategory", templateData.gensByCategory)

        return context
    }
}