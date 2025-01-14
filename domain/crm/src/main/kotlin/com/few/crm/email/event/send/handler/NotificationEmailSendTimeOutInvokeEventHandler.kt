package com.few.crm.email.event.send.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.few.crm.email.event.send.EmailSentEvent
import com.few.crm.email.event.send.NotificationEmailSendTimeOutInvokeEvent
import com.few.crm.email.repository.EmailTemplateRepository
import com.few.crm.email.repository.ScheduledEventRepository
import com.few.crm.email.service.CrmSendNonVariablesEmailService
import com.few.crm.email.service.NonContent
import com.few.crm.email.service.SendEmailArgs
import com.few.crm.support.jpa.CrmTransactional
import com.few.crm.user.repository.UserRepository
import event.EventHandler
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrElse

@Component
class NotificationEmailSendTimeOutInvokeEventHandler(
    private val scheduledEventRepository: ScheduledEventRepository,
    private val emailTemplateRepository: EmailTemplateRepository,
    private val userRepository: UserRepository,
    private val crmSendNonVariablesEmailService: CrmSendNonVariablesEmailService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) : EventHandler<NotificationEmailSendTimeOutInvokeEvent> {
    @CrmTransactional
    override fun handle(event: NotificationEmailSendTimeOutInvokeEvent) {
        scheduledEventRepository
            .findByEventIdAndCompletedFalseForUpdate(event.timeOutEventId)
            ?.complete() ?: return

        val templateId = event.templateId
        val userIds = event.userIds
        val template =
            emailTemplateRepository.findById(templateId).getOrElse {
                throw IllegalArgumentException("Email template not found with id $templateId")
            }
        val users = userRepository.findAllById(userIds)

        users.forEach { user ->
            val email = objectMapper.readValue(user.userAttributes, Map::class.java)["email"] as String
            val emailMessageId =
                crmSendNonVariablesEmailService.send(
                    SendEmailArgs(
                        to = email,
                        subject = template.subject,
                        template = template.body,
                        content = NonContent(),
                    ),
                )

            applicationEventPublisher.publishEvent(
                EmailSentEvent(
                    userExternalId = user.externalId!!,
                    emailBody = template.body,
                    destination = email,
                    messageId = emailMessageId,
                ),
            )
        }
    }
}