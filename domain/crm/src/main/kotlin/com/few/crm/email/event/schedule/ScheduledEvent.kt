package com.few.crm.email.event.schedule

import event.Event
import event.EventDetails
import event.EventUtils
import java.time.LocalDateTime

abstract class ScheduledEvent(
    eventId: String,
    eventType: String,
    eventTime: LocalDateTime,
) : Event(
        eventId,
        eventType,
        eventTime,
    )

@EventDetails(publishedLocations = ["com.few.crm.support.schedule.TimeOutEventTaskManager"])
class CancelScheduledEvent(
    val targetEventId: String,
    eventId: String = EventUtils.generateEventId(),
    eventTime: LocalDateTime = LocalDateTime.now(),
) : ScheduledEvent(
        eventId,
        "CancelScheduledEvent",
        eventTime,
    )