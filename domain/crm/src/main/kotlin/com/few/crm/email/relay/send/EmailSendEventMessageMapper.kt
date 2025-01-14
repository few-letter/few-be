package com.few.crm.email.relay.send

import com.few.crm.email.event.send.*
import event.message.MessageMapper
import event.message.MessagePayload
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

fun Map<String, Any>.messageId() = this["messageId"] as String

fun Map<String, Any>.destination() = this["destination"] as String

fun Map<String, Any>.timestamp() = this["timestamp"] as LocalDateTime

@Component
class EmailSendEventMessageMapper : MessageMapper<EmailSendEvent, EmailSendMessage>() {
    override fun map(event: EmailSendEvent): Optional<EmailSendMessage> = Optional.empty()

    fun to(messagePayload: MessagePayload): Optional<EmailSendEvent> =
        when (messagePayload.eventType!!.lowercase(Locale.getDefault())) {
            EmailSendStatus.OPEN.name.lowercase() ->
                Optional.of(
                    EmailOpenEvent(
                        messageId = messagePayload.data!!.messageId(),
                        destination = messagePayload.data!!.destination(),
                        timestamp = messagePayload.data!!.timestamp(),
                    ),
                )
            EmailSendStatus.DELIVERY.name.lowercase() ->
                Optional.of(
                    EmailDeliveryEvent(
                        messageId = messagePayload.data!!.messageId(),
                        destination = messagePayload.data!!.destination(),
                        timestamp = messagePayload.data!!.timestamp(),
                    ),
                )
            EmailSendStatus.CLICK.name.lowercase() ->
                Optional.of(
                    EmailClickEvent(
                        messageId = messagePayload.data!!.messageId(),
                        destination = messagePayload.data!!.destination(),
                        timestamp = messagePayload.data!!.timestamp(),
                    ),
                )
            EmailSendStatus.DELIVERYDELAY.name.lowercase() ->
                Optional.of(
                    EmailDeliveryDelayEvent(
                        messageId = messagePayload.data!!.messageId(),
                        destination = messagePayload.data!!.destination(),
                        timestamp = messagePayload.data!!.timestamp(),
                    ),
                )
            else -> Optional.empty()
        }
}