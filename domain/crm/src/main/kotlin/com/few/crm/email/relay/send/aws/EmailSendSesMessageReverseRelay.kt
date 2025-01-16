package com.few.crm.email.relay.send.aws

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.few.crm.config.CrmSqsConfig.Companion.SQS_LISTENER_CONTAINER_FACTORY
import com.few.crm.email.event.send.EmailSendEvent
import com.few.crm.email.relay.send.EmailSendEventMessageMapper
import com.few.crm.email.relay.send.EmailSendMessageReverseRelay
import com.few.crm.support.CrmApplicationEventPublisher
import com.few.crm.support.jpa.CrmTransactional
import event.EventUtils
import event.message.MessagePayload
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZonedDateTime

fun ObjectMapper.readMessage(message: String): JsonNode = readTree(message)["Message"]!!

fun JsonNode.mail(): JsonNode = this["mail"]

fun JsonNode.eventType(): String = this["eventType"].asText()

fun JsonNode.messageId(): String = this["messageId"].asText()

fun JsonNode.timestamp(): LocalDateTime = (this["timestamp"].asText()).let { ZonedDateTime.parse(it).toLocalDateTime() }

fun JsonNode.destination() = (this["destination"] as List<*>).joinToString(", ")

fun JsonNode.data() =
    mapOf(
        "messageId" to messageId(),
        "destination" to destination(),
        "timestamp" to timestamp(),
    )

@Profile("!local")
@Service
class EmailSendSesMessageReverseRelay(
    private val applicationEventPublisher: CrmApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
    private val eventMessageMapper: EmailSendEventMessageMapper,
) : EmailSendMessageReverseRelay() {
    val log = KotlinLogging.logger { }

    @CrmTransactional
    @SqsListener(queueNames = ["crm_ses_sqs"], factory = SQS_LISTENER_CONTAINER_FACTORY)
    fun onMessage(
        message: String,
        acknowledgement: Acknowledgement,
    ) {
        objectMapper
            .readMessage(message)
            .let {
                val mail = it.mail()
                MessagePayload(
                    eventId = EventUtils.generateEventId(),
                    eventType = it.eventType(),
                    eventTime = EventUtils.generateEventPublishedTime(),
                    data = mail.data(),
                )
            }.let {
                eventMessageMapper.to(it)
            }.ifPresent {
                publish(it)
            }
        acknowledgement.acknowledge()
    }

    override fun publish(event: EmailSendEvent) {
        log.info { "Publishing event: $event" }
        applicationEventPublisher.publishEventAndRecord(event)
    }
}