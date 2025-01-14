package event.fixtures

import event.EventDetails
import event.TimeExpiredEvent
import event.TimeOutEvent
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

@EventDetails(outBox = false)
class TestTimeOutEvent(
    expiredTime: LocalDateTime = LocalDateTime.now(),
    completed: Boolean = false,
    eventPublisher: ApplicationEventPublisher,
) : TimeOutEvent(
        expiredTime,
        completed,
        eventPublisher,
    ) {
    override fun timeExpiredEvent(): TimeExpiredEvent =
        TestTimeExpiredEvent(
            timeOutEventId = eventId,
        )
}

@EventDetails(outBox = false)
class TestTimeExpiredEvent(
    timeOutEventId: String,
) : TimeExpiredEvent(
        timeOutEventId,
    )