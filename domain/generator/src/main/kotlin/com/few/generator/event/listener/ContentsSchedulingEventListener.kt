package com.few.generator.event.listener

import com.few.generator.event.dto.ContentsSchedulingEventDto
import com.few.generator.event.handler.ContentsSchedulingHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ContentsSchedulingEventListener(
    private val contentsSchedulingHandler: ContentsSchedulingHandler,
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun handleEvent(event: ContentsSchedulingEventDto) {
        CoroutineScope(Dispatchers.IO).launch {
            contentsSchedulingHandler.handle(event)
        }
    }
}