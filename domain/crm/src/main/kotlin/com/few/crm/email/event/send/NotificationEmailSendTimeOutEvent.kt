package com.few.crm.email.event.send

import event.EventUtils
import event.TimeExpiredEvent
import event.TimeOutEvent
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class NotificationEmailSendTimeOutEvent(
    val templateId: Long,
    val userIds: List<Long>,
    eventId: String = EventUtils.generateEventId(),
    eventType: String,
    eventTime: LocalDateTime = LocalDateTime.now(),
    expiredTime: LocalDateTime,
    completed: Boolean = false,
    eventPublisher: ApplicationEventPublisher,
) : TimeOutEvent(
        eventId,
        eventType,
        eventTime,
        expiredTime,
        completed,
        eventPublisher,
    ) {
    override fun timeExpiredEvent(): TimeExpiredEvent =
        NotificationEmailSendTimeOutInvokeEvent(
            templateId = templateId,
            userIds = userIds,
            timeOutEventId = eventId,
            eventType = "ExpiredEvent",
        )
}

class NotificationEmailSendTimeOutInvokeEvent(
    val templateId: Long,
    val userIds: List<Long>,
    timeOutEventId: String,
    eventId: String = EventUtils.generateEventId(),
    eventType: String,
    eventTime: LocalDateTime = LocalDateTime.now(),
) : TimeExpiredEvent(
        timeOutEventId,
        eventId,
        eventType,
        eventTime,
    )