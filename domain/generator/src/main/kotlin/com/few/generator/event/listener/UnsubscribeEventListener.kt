package com.few.generator.event.listener

import com.few.generator.event.dto.UnsubscribeEventDto
import com.few.generator.event.handler.UnsubscribeHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class UnsubscribeEventListener(
    private val unsubscribeHandler: UnsubscribeHandler,
    private val notificationIoCoroutineScope: CoroutineScope,
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun handleEvent(event: UnsubscribeEventDto) {
        notificationIoCoroutineScope.launch {
            unsubscribeHandler.handle(event)
        }
    }
}