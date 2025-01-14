package com.few.crm.email.event.send

import event.Event
import event.EventDetails
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
) : Event()

@EventDetails
class EmailSentEvent(
    val userExternalId: String,
    val emailBody: String,
    messageId: String,
    destination: String,
    timestamp: LocalDateTime = LocalDateTime.now(),
) : EmailSendEvent(
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )

@EventDetails
class EmailDeliveryEvent(
    messageId: String,
    destination: String,
    timestamp: LocalDateTime,
) : EmailSendEvent(
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )

@EventDetails
class EmailOpenEvent(
    messageId: String,
    destination: String,
    timestamp: LocalDateTime,
) : EmailSendEvent(
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )

@EventDetails
class EmailClickEvent(
    messageId: String,
    destination: String,
    timestamp: LocalDateTime,
) : EmailSendEvent(
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )

@EventDetails
class EmailDeliveryDelayEvent(
    messageId: String,
    destination: String,
    timestamp: LocalDateTime,
) : EmailSendEvent(
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )