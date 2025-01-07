package com.few.crm.support.schedule

import com.few.crm.email.event.schedule.CancelScheduledEvent
import com.few.crm.email.event.send.NotificationEmailSendTimeOutEvent
import com.few.crm.support.toScheduleTime
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledFuture

class ManagedTask(
    val taskName: String,
    val task: ScheduledFuture<*>,
    val taskView: TaskView,
)

data class TaskView(
    val taskName: String,
    val values: Map<String, Any>,
)

@Component
class TimeOutEventTaskManager(
    private val taskScheduler: TaskScheduler,
    private val applicationEventPublisher: ApplicationEventPublisher,
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
        schedule(event)
        applicationEventPublisher.publishEvent(event)
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
            applicationEventPublisher.publishEvent(
                CancelScheduledEvent(
                    targetEventId = taskName,
                ),
            )
            log.info { "Task $taskName is cancelled" }
        }
    }

    fun scheduledTasksView(): List<TaskView> = tasks.values.map { it.taskView }
}