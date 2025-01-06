package com.few.crm.email.event.template

import com.few.crm.email.event.template.handler.PostEmailTemplateEventHandler
import com.few.crm.support.jpa.CrmTransactional
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class EventTemplateTransactionListener(
    private val postEmailTemplateEventHandler: PostEmailTemplateEventHandler,
) {
    @Async
    @CrmTransactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    fun handleAfterCompletionEvent(event: EmailTemplateTransactionAfterCompletionEvent) {
        when (event) {
            is PostEmailTemplateEvent -> postEmailTemplateEventHandler.handle(event)
        }
    }
}