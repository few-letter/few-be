package com.few.crm.support.schedule

import com.few.crm.email.event.schedule.CancelScheduledEvent
import com.few.crm.email.event.send.NotificationEmailSendTimeOutEvent
import com.few.crm.support.schedule.aws.AwsSchedulerService
import com.few.crm.support.schedule.aws.dto.NotificationEmailSendTimeOutEventInput
import com.few.crm.support.toScheduleTime
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.ScheduledFuture

class ManagedTask(
    val taskName: String,
    val task: ScheduledFuture<*>,
    val taskView: TaskView,
)

data class TaskView(
    val taskName: String,
    val values: Map<String, Any>,
) {
    init {
        require(values.isNotEmpty()) { "TaskView values must not be empty" }
        require((values["templateId"] != null) && (values["templateId"] is Long)) { "TaskView must have templateId" }
        require((values["userIds"] != null) && (values["userIds"] is List<*>)) { "TaskView must have userIds" }
        require((values["expiredTime"] != null) && (values["expiredTime"] is LocalDateTime)) { "TaskView must have expiredTime" }
    }
}

@Component
class TimeOutEventTaskManager(
    private val awsSchedulerService: AwsSchedulerService,
    private val taskScheduler: TaskScheduler,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @Value("\${crm.schedule.aws-min-minutes}") private val awsMinMinutes: Int,
) {
    val log = KotlinLogging.logger {}

    companion object {
        val tasks = mutableMapOf<String, ManagedTask>()
    }

    fun schedule(
        taskName: String,
        task: ScheduledFuture<*>,
        taskView: TaskView,
    ) {
        log.info { "Scheduling task $taskName" }
        tasks[taskName] = ManagedTask(taskName, task, taskView)
    }

    fun newSchedule(event: NotificationEmailSendTimeOutEvent) {
        if (event.isLongTermEvent(LocalDateTime.now().plusMinutes(awsMinMinutes.toLong()))) {
            val input =
                NotificationEmailSendTimeOutEventInput(
                    templateId = event.templateId,
                    userIds = event.userIds,
                    timeOutEventId = event.eventId,
                )
            awsSchedulerService.createSchedule(
                name = event.eventId,
                schedule = event.expiredTime,
                input = input,
            )
            applicationEventPublisher.publishEvent(event.toLongTermEvent())
        } else {
            schedule(event)
            applicationEventPublisher.publishEvent(event)
        }
    }

    fun reSchedule(event: NotificationEmailSendTimeOutEvent) {
        schedule(event)
    }

    private fun schedule(event: NotificationEmailSendTimeOutEvent) {
        this.schedule(
            event.eventId,
            taskScheduler.schedule(event, event.expiredTime.toScheduleTime()),
            TaskView(
                taskName = event.eventId,
                values =
                    mapOf(
                        "templateId" to event.templateId,
                        "userIds" to event.userIds,
                        "eventId" to event.eventId,
                        "eventType" to event.eventType,
                        "eventTime" to event.eventTime,
                        "expiredTime" to event.expiredTime,
                        "completed" to event.completed,
                    ),
            ),
        )
    }

    fun cancel(taskName: String) {
        tasks[taskName]?.let {
            it.task.cancel(false)
            tasks.remove(taskName)
        } ?: run {
            awsSchedulerService.deleteSchedule(taskName)
        }
        applicationEventPublisher.publishEvent(
            CancelScheduledEvent(
                targetEventId = taskName,
            ),
        )
        log.info { "Task $taskName is cancelled" }
    }

    fun scheduledTasksView(): List<TaskView> {
        val awsScheduleViews = awsSchedulerService.registeredScheduleView()
        return tasks.values.map { it.taskView } + awsScheduleViews
    }
}