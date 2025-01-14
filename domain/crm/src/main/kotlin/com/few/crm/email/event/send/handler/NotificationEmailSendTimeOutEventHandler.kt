package com.few.crm.email.event.send.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.few.crm.email.domain.ScheduledEvent
import com.few.crm.email.event.send.AwsNotificationEmailSendTimeOutEvent
import com.few.crm.email.event.send.NotificationEmailSendTimeOutEvent
import com.few.crm.email.event.send.NotificationEmailSendTimeOutEventType
import com.few.crm.email.repository.ScheduledEventRepository
import com.few.crm.support.jpa.CrmTransactional
import event.EventHandler
import org.springframework.stereotype.Component

@Component
class NotificationEmailSendTimeOutEventHandler(
    private val scheduledEventRepository: ScheduledEventRepository,
    private val objectMapper: ObjectMapper,
) : EventHandler<NotificationEmailSendTimeOutEvent> {
    @CrmTransactional
    override fun handle(event: NotificationEmailSendTimeOutEvent) {
        val scheduledAt =
            if (event is AwsNotificationEmailSendTimeOutEvent) {
                NotificationEmailSendTimeOutEventType.AWS.name
            } else {
                NotificationEmailSendTimeOutEventType.APP.name
            }

        scheduledEventRepository.save(
            ScheduledEvent(
                eventId = event.eventId,
                eventClass = event.javaClass.simpleName,
                eventPayload = objectMapper.writeValueAsString(event),
                completed = event.completed,
                scheduledAt = scheduledAt,
            ),
        )
    }
}