package com.few.crm.email.event.template.handler

import com.few.crm.email.domain.EmailTemplateHistory
import com.few.crm.email.event.template.PostEmailTemplateEvent
import com.few.crm.email.repository.EmailTemplateHistoryRepository
import com.few.crm.email.repository.EmailTemplateRepository
import com.few.crm.support.jpa.CrmTransactional
import event.EventHandler
import org.springframework.stereotype.Component

@Component
class PostEmailTemplateEventHandler(
    private val emailTemplateRepository: EmailTemplateRepository,
    private val emailTemplateHistoryRepository: EmailTemplateHistoryRepository,
) : EventHandler<PostEmailTemplateEvent> {
    @CrmTransactional
    override fun handle(event: PostEmailTemplateEvent) {
        val templateId = event.templateId
        val template =
            emailTemplateRepository
                .findById(templateId)
                .orElseThrow { IllegalArgumentException("EmailTemplate not found for id: $templateId") }
        emailTemplateHistoryRepository.save(
            EmailTemplateHistory(
                templateId = template.id!!,
                subject = template.subject,
                body = template.body,
                variables = template.variables,
                version = template.version,
            ),
        )
    }
}