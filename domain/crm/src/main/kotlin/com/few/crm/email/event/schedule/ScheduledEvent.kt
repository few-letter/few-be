package com.few.crm.email.event.schedule

import event.Event
import event.EventDetails

abstract class ScheduledEvent : Event()

@EventDetails
class CancelScheduledEvent(
    val targetEventId: String,
) : ScheduledEvent()