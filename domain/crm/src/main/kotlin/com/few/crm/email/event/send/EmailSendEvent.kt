package com.few.crm.email.event.send

import event.Event
import event.EventDetails
import event.EventUtils
import java.time.LocalDateTime

abstract class EmailSendEvent(
    eventId: String = EventUtils.generateEventId(),
    eventType: String,
    eventTime: LocalDateTime = LocalDateTime.now(),
    val messageId: String,
    val destination: String,
    val timestamp: LocalDateTime,
) : Event(
        eventId = eventId,
        eventType = eventType,
        eventTime = eventTime,
    ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EmailSendEvent

        if (messageId != other.messageId) return false
        if (destination != other.destination) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = messageId.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }

    override fun toString(): String = "EmailSendEvent(messageId='$messageId', destination='$destination', timestamp=$timestamp)"
}

@EventDetails(outBox = false)
class EmailSentEvent(
    val userExternalId: String,
    val emailBody: String,
    eventId: String = EventUtils.generateEventId(),
    eventType: String,
    eventTime: LocalDateTime = LocalDateTime.now(),
    messageId: String,
    destination: String,
    timestamp: LocalDateTime = LocalDateTime.now(),
) : EmailSendEvent(
        eventId = eventId,
        eventType = eventType,
        eventTime = eventTime,
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )

class EmailDeliveryEvent(
    eventId: String,
    eventType: String,
    eventTime: LocalDateTime,
    messageId: String,
    destination: String,
    timestamp: LocalDateTime,
) : EmailSendEvent(
        eventId = eventId,
        eventType = eventType,
        eventTime = eventTime,
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )

class EmailOpenEvent(
    eventId: String,
    eventType: String,
    eventTime: LocalDateTime,
    messageId: String,
    destination: String,
    timestamp: LocalDateTime,
) : EmailSendEvent(
        eventId = eventId,
        eventType = eventType,
        eventTime = eventTime,
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )

class EmailClickEvent(
    eventId: String,
    eventType: String,
    eventTime: LocalDateTime,
    messageId: String,
    destination: String,
    timestamp: LocalDateTime,
) : EmailSendEvent(
        eventId = eventId,
        eventType = eventType,
        eventTime = eventTime,
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )

class EmailDeliveryDelayEvent(
    eventId: String,
    eventType: String,
    eventTime: LocalDateTime,
    messageId: String,
    destination: String,
    timestamp: LocalDateTime,
) : EmailSendEvent(
        eventId = eventId,
        eventType = eventType,
        eventTime = eventTime,
        messageId = messageId,
        destination = destination,
        timestamp = timestamp,
    )