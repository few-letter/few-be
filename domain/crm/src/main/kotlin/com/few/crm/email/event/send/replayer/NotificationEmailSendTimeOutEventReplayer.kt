package com.few.crm.email.event.send.replayer

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.few.crm.email.event.send.NotificationEmailSendTimeOutEvent
import com.few.crm.email.repository.ScheduledEventRepository
import com.few.crm.support.LocalDateTimeExtension
import com.few.crm.support.jpa.CrmTransactional
import com.few.crm.support.parseEventTime
import com.few.crm.support.parseExpiredTime
import com.few.crm.support.schedule.TimeOutEventTaskManager
import event.EventRePlayer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

fun JsonNode.templateId() = this["templateId"].asLong()

fun JsonNode.userIds() = this["userIds"].map { it.asLong() }

fun JsonNode.eventTime() = LocalDateTimeExtension().parseEventTime(this["eventTime"].asText())

fun JsonNode.expiredTime() = LocalDateTimeExtension().parseExpiredTime(this["expiredTime"].asText())

@Component
class NotificationEmailSendTimeOutEventReplayer(
    private val eventScheduleRepository: ScheduledEventRepository,
    private val objectMapper: ObjectMapper,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val timeOutEventTaskManager: TimeOutEventTaskManager,
) : EventRePlayer(),
    ApplicationRunner {
    val log = KotlinLogging.logger {}

    @CrmTransactional
    override fun run(args: ApplicationArguments) {
        replay()
    }

    override fun replay() {
        log.info { "==================== [START] NotificationEmailSendTimeOutEventReplayer ====================" }
        eventScheduleRepository
            .findAllByCompletedFalse()
            .filter {
                it.eventClass == NotificationEmailSendTimeOutEvent::class.simpleName
            }.forEach {
                objectMapper.readTree(it.eventPayload).let { eventJson ->
                    val event =
                        NotificationEmailSendTimeOutEvent(
                            templateId = eventJson.templateId(),
                            userIds = eventJson.userIds(),
                            eventId = it.eventId,
                            eventType = it.eventClass,
                            eventTime = eventJson.eventTime(),
                            expiredTime = eventJson.expiredTime(),
                            completed = it.completed,
                            eventPublisher = applicationEventPublisher,
                        )
                    if (event.isExpired()) {
                        // TODO alert
                        log.error { "Event is expired. eventId: ${event.eventId} expiredTime: ${event.expiredTime}" }
                        eventScheduleRepository.findByEventId(event.eventId)?.isNotConsumed()?.complete()
                        return@forEach
                    }
                    log.info { "Event is replayed. eventId: ${event.eventId} expiredTime: ${event.expiredTime}" }
                    timeOutEventTaskManager.reSchedule(event)
                }
            }
        log.info { "==================== [END] NotificationEmailSendTimeOutEventReplayer ====================" }
    }
}