package event.domain

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.core.EventPublicationRepository

class DomainEventPublishingMethodInterceptor(
    private val method: DomainEventPublishingMethod,
    private val publisher: ApplicationEventPublisher,
    private val publicationRepository: EventPublicationRepository,
) : MethodInterceptor {
    override fun invoke(invocation: MethodInvocation): Any? {
        val result = invocation.proceed()

        if (result is DomainAbstractAggregateRoot<*>) {
            method.publishEventsFrom(listOf(result), publisher, publicationRepository)
        }
        return result
    }
}