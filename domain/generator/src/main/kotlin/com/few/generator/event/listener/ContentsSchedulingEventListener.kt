package com.few.generator.event.listener

import com.few.generator.event.ContentsSchedulingEvent
import com.few.generator.event.handler.ContentsSchedulingHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ContentsSchedulingEventListener(
    private val contentsSchedulingHandler: ContentsSchedulingHandler,
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun handleEvent(event: ContentsSchedulingEvent) {
        contentsSchedulingHandler.handle(event)
    }
}