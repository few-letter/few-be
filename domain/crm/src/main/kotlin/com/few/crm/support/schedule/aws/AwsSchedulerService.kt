package com.few.crm.support.schedule.aws

import com.fasterxml.jackson.databind.ObjectMapper
import com.few.crm.email.repository.ScheduledEventRepository
import com.few.crm.support.schedule.TaskView
import com.few.crm.support.schedule.aws.dto.NotificationEmailSendTimeOutEventInput
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.scheduler.SchedulerClient
import software.amazon.awssdk.services.scheduler.model.*
import software.amazon.awssdk.services.scheduler.model.Target
import java.time.LocalDateTime

fun LocalDateTime.toScheduleExpression(): String =
    "at(%04d-%02d-%02dT%02d:%02d:%02d)".format(
        this.year,
        this.monthValue,
        this.dayOfMonth,
        this.hour,
        this.minute,
        this.second,
    )

@Service
class AwsSchedulerService(
    private val scheduledEventRepository: ScheduledEventRepository,
    private val awsSchedulerClient: SchedulerClient,
    private val objectMapper: ObjectMapper,
    @Value("\${crm.schedule.roleArn}") private val roleArn: String,
    @Value("\${crm.schedule.sqsArn}") private val targetArn: String,
    @Value("\${crm.schedule.groupName}") private val groupName: String,
) {
    val log = KotlinLogging.logger {}

    fun createSchedule(
        name: String,
        schedule: LocalDateTime,
        input: NotificationEmailSendTimeOutEventInput,
    ) {
        val json = objectMapper.writeValueAsString(input)
        val target =
            Target
                .builder()
                .arn(targetArn)
                .roleArn(roleArn)
                .input(json)
                .build()

        val request: CreateScheduleRequest =
            CreateScheduleRequest
                .builder()
                .name(name)
                .scheduleExpression(schedule.toScheduleExpression())
                .scheduleExpressionTimezone("Asia/Seoul")
                .groupName(groupName)
                .description("This Schedule is created by CRM Application")
                .target(target)
                .actionAfterCompletion(ActionAfterCompletion.DELETE)
                .flexibleTimeWindow(
                    FlexibleTimeWindow
                        .builder()
                        .mode(FlexibleTimeWindowMode.OFF)
                        .build(),
                ).build()

        try {
            val response = awsSchedulerClient.createSchedule(request)
            log.info { "Successfully created schedule $name in schedule group $groupName The ARN is ${response.scheduleArn()}" }
        } catch (ex: ConflictException) {
            log.error { "A conflict exception occurred while creating the schedule: $ex.message" }
            throw RuntimeException("A conflict exception occurred while creating the schedule: ${ex.message}", ex)
        } catch (ex: Exception) {
            log.error { "Error creating schedule: ${ex.message}" }
            throw RuntimeException("Error creating schedule: ${ex.message}", ex)
        }
    }

    fun registeredScheduleView(): List<TaskView> {
        val awsSchedules =
            awsSchedulerClient
                .listSchedules(ListSchedulesRequest.builder().build())
                .schedules()
                .map { it.name() }
                .toList()

        return scheduledEventRepository
            .findAllByEventIdIn(awsSchedules)
            .map {
                val payload = objectMapper.readValue(it.eventPayload, Map::class.java).toMutableMap()
                payload["eventId"] = it.eventId
                payload
            }.map { payload ->
                TaskView(
                    taskName = payload["eventId"] as String,
                    values =
                        mapOf(
                            "templateId" to (payload["templateId"] as Int).toLong(),
                            "userIds" to (payload["userIds"] as List<Int>).map { it.toLong() },
                            "eventId" to payload["eventId"] as String,
                            "eventType" to payload["eventType"] as String,
                            "eventTime" to LocalDateTime.parse(payload["eventTime"] as String),
                            "expiredTime" to LocalDateTime.parse(payload["expiredTime"] as String),
                            "completed" to payload["completed"] as Boolean,
                        ),
                )
            }.toList()
    }

    fun deleteSchedule(eventId: String) {
        try {
            awsSchedulerClient.deleteSchedule(
                DeleteScheduleRequest
                    .builder()
                    .name(eventId)
                    .groupName(groupName)
                    .build(),
            )
            log.info { "Successfully deleted schedule $eventId" }
        } catch (ex: Exception) {
            log.error { "Error deleting schedule: ${ex.message}" }
            throw RuntimeException("Error deleting schedule: ${ex.message}", ex)
        }
    }
}