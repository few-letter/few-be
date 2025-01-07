package com.few.crm.email.event.schedule.handler

import com.few.crm.email.event.schedule.CancelScheduledEvent
import com.few.crm.email.repository.ScheduledEventRepository
import com.few.crm.support.jpa.CrmTransactional
import event.EventHandler
import org.springframework.stereotype.Component

@Component
class CancelScheduledEventHandler(
    private val scheduledEventRepository: ScheduledEventRepository,
) : EventHandler<CancelScheduledEvent> {
    @CrmTransactional
    override fun handle(event: CancelScheduledEvent) {
        scheduledEventRepository
            .findByEventId(event.targetEventId)
            ?.cancel() ?: throw IllegalStateException("Scheduled event not found for event id: ${event.eventId}")
    }
}