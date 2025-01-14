package com.few.crm.email.event.send

import event.EventDetails
import event.TimeExpiredEvent
import event.TimeOutEvent
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

enum class NotificationEmailSendTimeOutEventType {
    AWS,
    APP,
}

@EventDetails
open class NotificationEmailSendTimeOutEvent(
    val templateId: Long,
    val userIds: List<Long>,
    expiredTime: LocalDateTime,
    completed: Boolean = false,
    eventPublisher: ApplicationEventPublisher,
) : TimeOutEvent(
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
                eventPublisher = eventPublisher,
            )
        }
    }

    override fun timeExpiredEvent(): TimeExpiredEvent =
        NotificationEmailSendTimeOutInvokeEvent(
            templateId = templateId,
            userIds = userIds,
            timeOutEventId = eventId,
        )

    fun isLongTermEvent(now: LocalDateTime): Boolean = expiredTime.isAfter(now)

    fun toLongTermEvent(): AwsNotificationEmailSendTimeOutEvent =
        AwsNotificationEmailSendTimeOutEvent(
            templateId = templateId,
            userIds = userIds,
            expiredTime = expiredTime,
            completed = completed,
            eventPublisher = eventPublisher,
        )
}

@EventDetails
class NotificationEmailSendTimeOutInvokeEvent(
    val templateId: Long,
    val userIds: List<Long>,
    timeOutEventId: String,
) : TimeExpiredEvent(
        timeOutEventId,
    )

@EventDetails
class AwsNotificationEmailSendTimeOutEvent(
    templateId: Long,
    userIds: List<Long>,
    expiredTime: LocalDateTime,
    completed: Boolean = false,
    eventPublisher: ApplicationEventPublisher,
) : NotificationEmailSendTimeOutEvent(
        templateId,
        userIds,
        expiredTime,
        completed,
        eventPublisher,
    )