package com.few.crm.email.event.send

import event.EventUtils
import event.TimeExpiredEvent
import event.TimeOutEvent
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

open class NotificationEmailSendTimeOutEvent(
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
    companion object {
        fun new(
            templateId: Long,
            userIds: List<Long>,
            expiredTime: LocalDateTime,
            eventPublisher: ApplicationEventPublisher,
        ): NotificationEmailSendTimeOutEvent {
            if (LocalDateTime.now().isAfter(expiredTime)) {
                throw IllegalArgumentException("Expired time must be after current time")
            }
            return NotificationEmailSendTimeOutEvent(
                templateId = templateId,
                userIds = userIds,
                expiredTime = expiredTime,
                eventType = "TimeOutEvent",
                eventPublisher = eventPublisher,
            )
        }
    }

    override fun timeExpiredEvent(): TimeExpiredEvent =
        NotificationEmailSendTimeOutInvokeEvent(
            templateId = templateId,
            userIds = userIds,
            timeOutEventId = eventId,
            eventType = "ExpiredEvent",
        )

    fun isLongTermEvent(now: LocalDateTime): Boolean = expiredTime.isAfter(now)

    fun toLongTermEvent(): AwsNotificationEmailSendTimeOutEvent =
        AwsNotificationEmailSendTimeOutEvent(
            templateId = templateId,
            userIds = userIds,
            eventId = eventId,
            eventType = eventType,
            eventTime = eventTime,
            expiredTime = expiredTime,
            completed = completed,
            eventPublisher = eventPublisher,
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

class AwsNotificationEmailSendTimeOutEvent(
    templateId: Long,
    userIds: List<Long>,
    eventId: String = EventUtils.generateEventId(),
    eventType: String,
    eventTime: LocalDateTime = LocalDateTime.now(),
    expiredTime: LocalDateTime,
    completed: Boolean = false,
    eventPublisher: ApplicationEventPublisher,
) : NotificationEmailSendTimeOutEvent(
        templateId,
        userIds,
        eventId,
        eventType,
        eventTime,
        expiredTime,
        completed,
        eventPublisher,
    )