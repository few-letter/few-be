package com.few.crm.email.event.send.handler

import com.few.crm.email.event.send.EmailDeliveryEvent
import com.few.crm.email.event.send.EmailSendStatus
import com.few.crm.email.repository.EmailSendHistoryRepository
import com.few.crm.support.jpa.CrmTransactional
import event.EventHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class EmailDeliveryEventHandler(
    private val emailSendHistoryRepository: EmailSendHistoryRepository,
) : EventHandler<EmailDeliveryEvent> {
    val log = KotlinLogging.logger {}

    @CrmTransactional
    override fun handle(event: EmailDeliveryEvent) {
        log.info { "Handling EmailDeliveryEvent: $event" }
        // TODO check emailSendHistory and update status if history is not found retry 3 times
        emailSendHistoryRepository
            .findByEmailMessageId(event.messageId)
            ?.let {
                it.sendStatus = EmailSendStatus.DELIVERY.name
                emailSendHistoryRepository.save(it)
            }
            ?: log.error { "EmailSendHistory not found for messageId: ${event.messageId}" }
    }
}