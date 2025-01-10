package com.few.crm.email.domain

import com.few.crm.email.event.send.EmailSentEvent
import event.domain.DomainAbstractAggregateRoot
import org.jmolecules.ddd.annotation.AggregateRoot

@AggregateRoot
class SentEmail(
    private val userExternalId: String,
    private val emailBody: String,
    private val destination: String,
    private val emailMessageId: String,
    private val eventType: EmailSendEventType = EmailSendEventType.SEND,
) : DomainAbstractAggregateRoot<SentEmail>() {
    init {
        registerEvent(
            EmailSentEvent(
                userExternalId = userExternalId,
                emailBody = emailBody,
                messageId = emailMessageId,
                destination = destination,
                eventType = eventType.name,
            ),
        )
    }
}