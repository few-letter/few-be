package com.few.crm.email.service

import com.few.crm.email.domain.EmailSendEventType
import com.few.crm.email.domain.SentEmail
import email.*
import email.provider.CrmAwsSESEmailSendProvider
import event.domain.PublishEvents
import org.springframework.boot.autoconfigure.mail.MailProperties
import org.springframework.stereotype.Component

data class SendEmailArgs(
    override val to: String,
    override val subject: String,
    override val template: String,
    override val content: NonContent,
    override val properties: String = "",
) : SendMailArgs<NonContent, String>

data class SendEmailDto(
    val to: String,
    val subject: String,
    val template: String,
    val content: NonContent,
    val properties: String = "",
    val userExternalId: String,
    val emailBody: String,
    val destination: String,
    val eventType: EmailSendEventType,
) {
    val emailArgs = SendEmailArgs(to, subject, template, content, properties)
}

@Component
class CrmSendNonVariablesEmailService(
    mailProperties: MailProperties,
    emailSendProvider: CrmAwsSESEmailSendProvider,
    private val emailTemplateProcessor: EmailTemplateProcessor,
) : EmailSender<SendEmailArgs>(mailProperties, emailSendProvider) {
    override fun getHtml(args: SendEmailArgs): String {
        val context = EmailContext()
        return emailTemplateProcessor.process(args.template, context, EmailTemplateType.STRING)
    }

    @PublishEvents
    fun send(args: SendEmailDto): SentEmail {
        val emailArgs = args.emailArgs
        return SentEmail(
            userExternalId = args.userExternalId,
            emailBody = args.emailBody,
            destination = args.destination,
            emailMessageId = send(emailArgs),
            eventType = args.eventType,
        )
    }
}

class NonContent