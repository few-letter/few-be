package com.few.generator.service.specifics.newsletter

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
        context.setVariable("webLink", "https://few-fe.vercel.app/")
        context.setVariable("instaLink", "https://www.instagram.com/few.letter?igsh=MXdla291OWRndG84aw==&utm_source=qr")

        return context
    }
}