package com.few.crm.email.event.send

import com.few.crm.config.CrmThreadPoolConfig.Companion.CRM_LISTENER_POOL
import com.few.crm.email.event.send.handler.*
import com.few.crm.email.relay.send.EmailSendEventMessageMapper
import com.few.crm.support.jpa.CrmTransactional
import org.springframework.context.event.EventListener
import org.springframework.modulith.events.core.EventPublicationRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.event.TransactionalEventListener
import java.time.Instant
import java.util.*

@Component
class EmailSendEventListener(
    private val emailSentEventHandler: EmailSentEventHandler,
    private val emailDeliveryEventHandler: EmailDeliveryEventHandler,
    private val emailOpenEventHandler: EmailOpenEventHandler,
    private val emailClickEventHandler: EmailClickEventHandler,
    private val emailDeliveryDelayEventHandler: EmailDeliveryDelayEventHandler,
    private val emailSendEventMessageMapper: EmailSendEventMessageMapper,
    private val eventPublicationRepository: EventPublicationRepository,
) {
    @Async(CRM_LISTENER_POOL)
    @EventListener
    @CrmTransactional(propagation = Propagation.REQUIRES_NEW)
    fun onEvent(event: EmailSendEvent) {
        when (event) {
            is EmailSentEvent -> emailSentEventHandler.handle(event)
            is EmailDeliveryEvent -> emailDeliveryEventHandler.handle(event)
            is EmailOpenEvent -> emailOpenEventHandler.handle(event)
            is EmailClickEvent -> emailClickEventHandler.handle(event)
            is EmailDeliveryDelayEvent -> emailDeliveryDelayEventHandler.handle(event)
        }

        eventPublicationRepository.markCompleted(UUID.fromString(event.eventId), Instant.now())
    }

    @Async(CRM_LISTENER_POOL)
    @TransactionalEventListener
    @CrmTransactional(propagation = Propagation.REQUIRES_NEW)
    fun onInCompleteEvent(event: EmailSendEvent) {
        when (event) {
            is EmailSentEvent -> emailSentEventHandler.handle(event)
            is EmailDeliveryEvent -> emailDeliveryEventHandler.handle(event)
            is EmailOpenEvent -> emailOpenEventHandler.handle(event)
            is EmailClickEvent -> emailClickEventHandler.handle(event)
            is EmailDeliveryDelayEvent -> emailDeliveryDelayEventHandler.handle(event)
        }

        eventPublicationRepository.markCompleted(UUID.fromString(event.eventId), Instant.now())
    }
}