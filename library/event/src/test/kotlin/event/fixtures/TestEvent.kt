package event.fixtures

import event.Event
import event.EventDetails

@EventDetails(outBox = true)
class TestEvent : Event()