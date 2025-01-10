package com.few.crm.email.usecase

import com.few.crm.email.domain.EmailTemplate
import com.few.crm.email.domain.EmailTemplateHistory
import com.few.crm.email.repository.EmailTemplateHistoryRepository
import com.few.crm.email.repository.EmailTemplateRepository
import com.few.crm.email.service.HtmlValidator
import com.few.crm.email.usecase.dto.PostTemplateUseCaseIn
import com.few.crm.email.usecase.dto.PostTemplateUseCaseOut
import com.few.crm.support.jpa.CrmTransactional
import org.springframework.stereotype.Service

@Service
class PostTemplateUseCase(
    private val emailTemplateRepository: EmailTemplateRepository,
    private val emailTemplateHistoryRepository: EmailTemplateHistoryRepository,
    private val htmlValidator: HtmlValidator,
) {
    @CrmTransactional
    fun execute(useCaseIn: PostTemplateUseCaseIn): PostTemplateUseCaseOut {
        val id: Long? = useCaseIn.id
        val templateName = useCaseIn.templateName
        val subject: String? = useCaseIn.subject
        val version: Float? = useCaseIn.version
        var body = useCaseIn.body
        if (!htmlValidator.isValidHtml(body)) {
            throw IllegalArgumentException("Invalid HTML")
        }
        body = htmlValidator.prettyPrintHtml(body)!!
        val bodyVariables: List<String> = htmlValidator.extractVariables(body).sorted()

        val variables =
            useCaseIn.variables
                .filterNot {
                    it.isBlank()
                }.filterNot {
                    it.isEmpty()
                }.sorted()

        if (bodyVariables != variables) {
            throw IllegalArgumentException("Variables do not match")
        }

        val persistedTemplate: EmailTemplate? = getEmailTemplate(id, templateName)

        val modifiedOrNewTemplate =
            persistedTemplate
                ?.modify()
                ?.modifySubject(subject)
                ?.modifyBody(body, variables)
                ?.done()
                ?: run {
                    EmailTemplate.new(
                        templateName = templateName,
                        subject = subject!!,
                        body = body,
                        variables = variables,
                    )
                }

        modifiedOrNewTemplate.let { template ->
            if (template.isNewTemplate()) {
                emailTemplateRepository.save(template)
            } else {
                template.updateVersion(version)
            }
        }

        return run {
            PostTemplateUseCaseOut(
                id = modifiedOrNewTemplate.id!!,
                templateName = modifiedOrNewTemplate.templateName,
                version = modifiedOrNewTemplate.version,
            )
        }
    }

    private fun getEmailTemplate(
        id: Long?,
        templateName: String,
    ): EmailTemplate? {
        if (id != null) {
            return emailTemplateRepository
                .findById(id)
                .orElseThrow { IllegalArgumentException("Template not found") }
        }

        return emailTemplateRepository
            .findByTemplateName(templateName)
            ?.let {
                throw IllegalArgumentException("Duplicate template name: $templateName")
            }
    }
}