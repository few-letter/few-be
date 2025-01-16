package com.few.crm.support

import event.Event
import event.domain.EventPublication
import event.domain.TraceAbleEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.core.EventPublicationRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CrmApplicationEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val eventPublicationRepository: EventPublicationRepository,
) {
    fun publishEventAndRecord(event: TraceAbleEvent) {
        applicationEventPublisher.publishEvent(event)
        eventPublicationRepository.create(
            EventPublication(
                event = event,
                targetIdentifier = event.targetIdentifier,
                publicationDate = Instant.now(),
            ),
        )
    }

    fun publishEvent(event: Event) {
        applicationEventPublisher.publishEvent(event)
    }
}