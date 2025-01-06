package com.few.crm.email.event.send.handler

import com.few.crm.email.event.send.EmailOpenEvent
import com.few.crm.email.repository.EmailSendHistoryRepository
import event.EventHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*

@Component
class EmailOpenEventHandler(
    private val emailSendHistoryRepository: EmailSendHistoryRepository,
) : EventHandler<EmailOpenEvent> {
    val logger = KotlinLogging.logger {}

    override fun handle(event: EmailOpenEvent) {
        logger.info { "Handling EmailOpenEvent: $event" }
        // TODO check emailSendHistory and update status if history is not found retry 3 times
        emailSendHistoryRepository
            .findByEmailMessageId(event.messageId)
            ?.let {
                it.sendStatus = event.eventType.uppercase(Locale.getDefault())
                emailSendHistoryRepository.save(it)
            }
            ?: logger.error { "EmailSendHistory not found for messageId: ${event.messageId}" }
    }
}