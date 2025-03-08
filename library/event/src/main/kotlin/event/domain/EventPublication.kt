package event.domain

import event.Event
import org.springframework.modulith.events.core.PublicationTargetIdentifier
import org.springframework.modulith.events.core.TargetEventPublication
import java.time.Instant
import java.util.*

class EventPublication(
    private val event: Event,
    private val targetIdentifier: PublicationTargetIdentifier,
    private val publicationDate: Instant,
    private var completionDate: Optional<Instant> = Optional.empty(),
) : TargetEventPublication {
    override fun markCompleted(instant: Instant) {
        completionDate = Optional.of(instant)
    }

    override fun getIdentifier(): UUID = UUID.fromString(event.eventId)

    override fun getEvent(): Any = event

    override fun getPublicationDate(): Instant = publicationDate

    override fun getCompletionDate(): Optional<Instant> = completionDate

    override fun getTargetIdentifier(): PublicationTargetIdentifier = targetIdentifier
}