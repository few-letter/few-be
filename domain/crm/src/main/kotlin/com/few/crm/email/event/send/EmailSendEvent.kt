package com.few.crm.email.event.send

import event.EventDetails
import event.domain.TraceAbleEvent
import org.springframework.modulith.events.core.PublicationTargetIdentifier
import java.time.LocalDateTime

enum class EmailSendStatus {
    SENT,
    OPEN,
    DELIVERY,
    CLICK,
    DELIVERYDELAY,
}

abstract class EmailSendEvent(
    val messageId: String,
    val destination: String,
    val timestamp: LocalDateTime,
    targetIdentifier: PublicationTargetIdentifier,
) : TraceAbleEvent(
        targetIdentifier = targetIdentifier,
    ) {
    companion object {
        val PUBLICATION_TARGET_IDENTIFIER_LOCATION: PublicationTargetIdentifier =
            PublicationTargetIdentifier.of(
                "com.few.crm.email.event.send.EmailSendEventListener.onInCompleteEvent(${EmailSendEvent::class.qualifiedName})",
            )
    }
}

@EventDetails
class EmailSentEvent(
    val userExternalId: String,
    val emailBody: String,
    targetIdentifier: PublicationTargetIdentifier = PUBLICATION_TARGET_IDENTIFIER_LOCATION,
    messageId: String,
    destination: String,
    timestamp: LocalDateTime = LocalDateTime.now(),
) : EmailSendEvent(
        targetIdentifier = targetIdentifier,
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )

@EventDetails
class EmailDeliveryEvent(
    targetIdentifier: PublicationTargetIdentifier = PUBLICATION_TARGET_IDENTIFIER_LOCATION,
    messageId: String,
    destination: String,
    timestamp: LocalDateTime,
) : EmailSendEvent(
        targetIdentifier = targetIdentifier,
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )

@EventDetails
class EmailOpenEvent(
    targetIdentifier: PublicationTargetIdentifier = PUBLICATION_TARGET_IDENTIFIER_LOCATION,
    messageId: String,
    destination: String,
    timestamp: LocalDateTime,
) : EmailSendEvent(
        targetIdentifier = targetIdentifier,
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )

@EventDetails
class EmailClickEvent(
    targetIdentifier: PublicationTargetIdentifier = PUBLICATION_TARGET_IDENTIFIER_LOCATION,
    messageId: String,
    destination: String,
    timestamp: LocalDateTime,
) : EmailSendEvent(
        targetIdentifier = targetIdentifier,
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )

@EventDetails
class EmailDeliveryDelayEvent(
    targetIdentifier: PublicationTargetIdentifier = PUBLICATION_TARGET_IDENTIFIER_LOCATION,
    messageId: String,
    destination: String,
    timestamp: LocalDateTime,
) : EmailSendEvent(
        targetIdentifier = targetIdentifier,
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )