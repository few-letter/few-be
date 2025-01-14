package com.few.crm.email.event.send.handler

import com.few.crm.email.event.send.EmailDeliveryDelayEvent
import com.few.crm.support.jpa.CrmTransactional
import event.EventHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

@Component
class EmailDeliveryDelayEventHandler : EventHandler<EmailDeliveryDelayEvent> {
    val logger = KotlinLogging.logger {}

    @CrmTransactional
    override fun handle(event: EmailDeliveryDelayEvent) {
        logger.info { "Handling EmailDeliveryDelayEvent: $event" }
    }
}