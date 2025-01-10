package event.fixtures

import event.Event
import event.EventDetails
import event.EventUtils
import java.time.LocalDateTime

@EventDetails(outBox = true)
class TestEvent(
    eventId: String = EventUtils.generateEventId(),
    eventType: String,
    eventTime: LocalDateTime = LocalDateTime.now(),
) : Event(
        eventId,
        eventType,
        eventTime,
    )