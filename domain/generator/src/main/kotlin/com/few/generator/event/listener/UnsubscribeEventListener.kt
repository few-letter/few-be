package com.few.generator.event.listener

import com.few.generator.event.UnsubscribeEvent
import com.few.generator.event.handler.UnsubscribeHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class UnsubscribeEventListener(
    private val unsubscribeHandler: UnsubscribeHandler,
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun handleEvent(event: UnsubscribeEvent) {
        unsubscribeHandler.handle(event)
    }
}