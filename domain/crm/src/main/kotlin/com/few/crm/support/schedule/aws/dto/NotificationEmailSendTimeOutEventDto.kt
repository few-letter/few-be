package com.few.crm.support.schedule.aws.dto

class NotificationEmailSendTimeOutEventDto

data class NotificationEmailSendTimeOutEventInput(
    val templateId: Long,
    val userIds: List<Long>,
    val timeOutEventId: String,
)