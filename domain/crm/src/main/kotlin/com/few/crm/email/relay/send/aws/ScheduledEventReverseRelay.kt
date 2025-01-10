package com.few.crm.email.relay.send.aws

import com.fasterxml.jackson.databind.ObjectMapper
import com.few.crm.config.CrmSqsConfig.Companion.SQS_LISTENER_CONTAINER_FACTORY
import com.few.crm.email.event.send.NotificationEmailSendTimeOutInvokeEvent
import event.message.MessageReverseRelay
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("!local")
@Service
class ScheduledEventReverseRelay(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) : MessageReverseRelay<NotificationEmailSendTimeOutInvokeEvent> {
    val log = KotlinLogging.logger { }

    /**
     * @param message [com.few.crm.support.schedule.aws.dto.NotificationEmailSendTimeOutEventInput]
     */
    @SqsListener(queueNames = ["crm_schedule_event_sqs"], factory = SQS_LISTENER_CONTAINER_FACTORY)
    fun onMessage(
        message: String,
        acknowledgement: Acknowledgement,
    ) {
        objectMapper.readTree(message).let { jsonNode ->
            val templateId = jsonNode["templateId"].asLong()
            val userIds = jsonNode["userIds"].map { it.asLong() }
            val eventTime = jsonNode["timeOutEventId"].asText()
            NotificationEmailSendTimeOutInvokeEvent(
                templateId = templateId,
                userIds = userIds,
                timeOutEventId = eventTime,
                eventType = "AWSINVOKE",
            ).let { event ->
                publish(event)
            }
        }
        acknowledgement.acknowledge()
    }

    override fun publish(event: NotificationEmailSendTimeOutInvokeEvent) {
        log.info { "Publishing event: $event" }
        applicationEventPublisher.publishEvent(event)
    }
}