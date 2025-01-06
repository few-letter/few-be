package com.few.crm.email.event.template

import event.Event
import event.EventUtils

abstract class EmailTemplateTransactionEvent(
    eventId: String = EventUtils.generateEventId(),
    eventType: String,
    eventTime: Long = System.currentTimeMillis(),
) : Event(
        eventId = eventId,
        eventType = eventType,
        eventTime = eventTime,
    )

abstract class EmailTemplateTransactionAfterCompletionEvent(
    eventId: String = EventUtils.generateEventId(),
    eventType: String,
    eventTime: Long = System.currentTimeMillis(),
) : EmailTemplateTransactionEvent(
        eventId = eventId,
        eventType = eventType,
        eventTime = eventTime,
    )

class PostEmailTemplateEvent(
    val templateId: Long,
    eventId: String = EventUtils.generateEventId(),
    eventType: String,
    eventTime: Long = System.currentTimeMillis(),
) : EmailTemplateTransactionAfterCompletionEvent(
        eventType = "PostEmailTemplateEvent",
    ) {
    override fun getData(): Map<String, Any> =
        mapOf(
            "templateId" to templateId,
        )
}