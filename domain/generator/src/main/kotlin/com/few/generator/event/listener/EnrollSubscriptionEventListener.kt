package com.few.generator.event.listener

import com.few.generator.event.EnrollSubscriptionEvent
import com.few.generator.event.handler.EnrollSubscriptionHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class EnrollSubscriptionEventListener(
    private val enrollSubscriptionHandler: EnrollSubscriptionHandler,
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun handleEvent(event: EnrollSubscriptionEvent) {
        enrollSubscriptionHandler.handle(event)
    }
}