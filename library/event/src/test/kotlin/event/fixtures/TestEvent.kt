package event.fixtures

import event.Event
import event.EventDetails
import java.time.LocalDateTime

@EventDetails(outBox = true)
class TestEvent(
    eventId: String,
    eventTime: LocalDateTime,
) : Event(
        eventId,
        eventTime,
    )
