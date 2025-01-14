package com.few.crm.email.event.send

import com.few.crm.config.CrmThreadPoolConfig.Companion.CRM_LISTENER_POOL
import com.few.crm.email.event.send.handler.NotificationEmailSendTimeOutEventHandler
import com.few.crm.email.event.send.handler.NotificationEmailSendTimeOutInvokeEventHandler
import com.few.crm.support.jpa.CrmTransactional
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation

@Component
class NotificationEmailSendTimeOutEventListener(
    private val notificationEmailSendTimeOutEventHandler: NotificationEmailSendTimeOutEventHandler,
    private val notificationEmailSendTimeOutInvokeEventHandler: NotificationEmailSendTimeOutInvokeEventHandler,
) {
    @Async(CRM_LISTENER_POOL)
    @EventListener
    @CrmTransactional(propagation = Propagation.REQUIRES_NEW)
    fun onEvent(event: NotificationEmailSendTimeOutEvent) {
        notificationEmailSendTimeOutEventHandler.handle(event)
    }

    @Async
    @EventListener
    @CrmTransactional(propagation = Propagation.REQUIRES_NEW)
    fun onEvent(event: NotificationEmailSendTimeOutInvokeEvent) {
        notificationEmailSendTimeOutInvokeEventHandler.handle(event)
    }
}