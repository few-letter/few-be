package com.few.crm.email.domain

import com.few.crm.email.event.template.PostEmailTemplateEvent
import com.few.crm.support.jpa.converter.StringListConverter
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.domain.AbstractAggregateRoot
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(name = "email_templates")
@EntityListeners(AuditingEntityListener::class)
class EmailTemplate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "template_name")
    var templateName: String,
    @Column(name = "subject")
    var subject: String,
    @Lob
    @Column(name = "body", columnDefinition = "BLOB")
    var body: String,
    @Convert(converter = StringListConverter::class)
    @Column(name = "variables")
    var variables: List<String> = listOf(),
    @Column(name = "version")
    var version: Float = 1.0f,
    @CreatedDate
    var createdAt: LocalDateTime? = null,
) : AbstractAggregateRoot<EmailTemplate>() {
    protected constructor() : this(
        templateName = "",
        subject = "",
        body = "",
        variables = emptyList(),
    )

    companion object {
        fun new(
            templateName: String,
            subject: String,
            body: String,
            variables: List<String>,
        ): EmailTemplate =
            EmailTemplate(
                templateName = templateName,
                subject = subject,
                body = body,
                variables = variables,
            )
    }

    fun isNewTemplate(): Boolean = id == null

    private fun registerModifyEvent() {
        registerEvent(
            PostEmailTemplateEvent(
                templateId = this.id!!,
                eventType = "POST",
            ),
        )
    }

    fun modify(): EmailTemplateModifyBuilder = EmailTemplateModifyBuilder(this)

    class EmailTemplateModifyBuilder(
        private val template: EmailTemplate,
        private var isVersionUpdated: Boolean = false,
    ) {
        fun modifySubject(subject: String?): EmailTemplateModifyBuilder {
            subject?.let {
                template.subject = subject
            }
            return this
        }

        fun modifyBody(
            body: String,
            variables: List<String>,
        ): EmailTemplateModifyBuilder {
            template.body = body
            template.variables = variables
            return this
        }

        fun updateVersion(version: Float?): EmailTemplateModifyBuilder {
            version?.let {
                if (it <= template.version) {
                    throw IllegalArgumentException("Invalid version: $it")
                }
                this.template.version = it
            } ?: kotlin.run {
                this.template.version += 0.1f
            }
            isVersionUpdated = true
            return this
        }

        fun done(): EmailTemplate {
            if (!isVersionUpdated) {
                updateVersion(null)
            }
            template.registerModifyEvent()
            return template
        }
    }
}