package com.few.crm.email.event.template

import event.Event
import event.EventDetails

abstract class EmailTemplateTransactionEvent : Event()

abstract class EmailTemplateTransactionAfterCommitEvent : EmailTemplateTransactionEvent()

@EventDetails
class PostEmailTemplateEvent(
    val templateId: Long,
) : EmailTemplateTransactionAfterCommitEvent()