package com.few.crm.email.event.template

import event.Event
import event.EventDetails
import event.EventUtils
import java.time.LocalDateTime

abstract class EmailTemplateTransactionEvent(
    eventId: String = EventUtils.generateEventId(),
    eventType: String,
    eventTime: LocalDateTime = LocalDateTime.now(),
) : Event(
        eventId = eventId,
        eventType = eventType,
        eventTime = eventTime,
    )

abstract class EmailTemplateTransactionAfterCompletionEvent(
    eventId: String = EventUtils.generateEventId(),
    eventType: String,
    eventTime: LocalDateTime = LocalDateTime.now(),
) : EmailTemplateTransactionEvent(
        eventId = eventId,
        eventType = eventType,
        eventTime = eventTime,
    )

@EventDetails(publishedLocations = ["com.few.crm.email.domain.EmailTemplate"])
class PostEmailTemplateEvent(
    val templateId: Long,
    eventId: String = EventUtils.generateEventId(),
    eventType: String,
    eventTime: LocalDateTime = LocalDateTime.now(),
) : EmailTemplateTransactionAfterCompletionEvent(
        eventId = eventId,
        eventType = eventType,
        eventTime = eventTime,
    )