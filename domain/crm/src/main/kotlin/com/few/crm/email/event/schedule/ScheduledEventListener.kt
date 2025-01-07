package com.few.crm.email.event.schedule

import com.few.crm.email.event.schedule.handler.CancelScheduledEventHandler
import com.few.crm.support.jpa.CrmTransactional
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation

@Component
class ScheduledEventListener(
    private val cancelScheduledEventHandler: CancelScheduledEventHandler,
) {
    @Async
    @EventListener
    @CrmTransactional(propagation = Propagation.REQUIRES_NEW)
    fun onCancelEvent(event: CancelScheduledEvent) {
        cancelScheduledEventHandler.handle(event)
    }
}