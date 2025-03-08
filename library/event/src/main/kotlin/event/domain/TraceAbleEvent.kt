package event.domain

import event.Event
import event.EventUtils
import org.springframework.modulith.events.core.PublicationTargetIdentifier
import java.time.LocalDateTime

abstract class TraceAbleEvent(
    val targetIdentifier: PublicationTargetIdentifier,
    eventId: String = EventUtils.generateEventId(),
    eventTime: LocalDateTime = EventUtils.generateEventPublishedTime(),
) : Event(
        eventId,
        eventTime,
    )