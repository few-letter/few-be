package com.few.crm.email.event.send.handler

import com.few.crm.email.event.send.EmailClickEvent
import com.few.crm.email.event.send.EmailSendStatus
import com.few.crm.email.repository.EmailSendHistoryRepository
import com.few.crm.support.jpa.CrmTransactional
import event.EventHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class EmailClickEventHandler(
    private val emailSendHistoryRepository: EmailSendHistoryRepository,
) : EventHandler<EmailClickEvent> {
    val logger = KotlinLogging.logger {}

    @CrmTransactional
    override fun handle(event: EmailClickEvent) {
        logger.info { "Handling EmailClickEvent: $event" }
        // TODO check emailSendHistory and update status if history is not found retry 3 times
        emailSendHistoryRepository
            .findByEmailMessageId(event.messageId)
            ?.let {
                it.sendStatus = EmailSendStatus.CLICK.name
                emailSendHistoryRepository.save(it)
            }
            ?: logger.error { "EmailSendHistory not found for messageId: ${event.messageId}" }
    }
}