package com.few.email

import com.few.email.provider.EmailSendProvider
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.stereotype.Component

data class GenNewsletterContent(
    val gens: List<GenData>,
)

data class GenData(
    val id: Long,
    val headline: String,
    val summary: String,
    val category: Int,
)

data class GenNewsletterArgs(
    override val to: String,
    override val subject: String,
    override val template: String = "newsletter",
    override val content: GenNewsletterContent,
    override val properties: Map<String, Any> = emptyMap(),
    val emailContext: EmailContext,
) : SendMailArgs<GenNewsletterContent, Map<String, Any>>

@Component
class GenNewsletterSender(
    mailProperties: MailProperties,
    defaultEmailSendProvider: EmailSendProvider,
    private val emailTemplateProcessor: EmailTemplateProcessor,
) : EmailSender<GenNewsletterArgs>(mailProperties, defaultEmailSendProvider) {
    override fun getHtml(args: GenNewsletterArgs): String {
        // 전달받은 EmailContext를 사용
        return emailTemplateProcessor.process("newsletter", args.emailContext, EmailTemplateType.HTML)
    }
}