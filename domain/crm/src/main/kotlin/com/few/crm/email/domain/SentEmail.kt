package com.few.crm.email.domain

import com.few.crm.email.event.send.EmailSentEvent
import org.springframework.data.domain.AbstractAggregateRoot

class SentEmail(
    private val userExternalId: String,
    private val emailBody: String,
    private val destination: String,
    private val emailMessageId: String,
    private val eventType: EmailSendEventType = EmailSendEventType.SEND,
) : AbstractAggregateRoot<SentEmail>() {
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