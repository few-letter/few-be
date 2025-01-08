package event.domain

/**
 * Domain abstract aggregate root
 *
 * @see org.springframework.data.domain.AbstractAggregateRoot
 */
abstract class DomainAbstractAggregateRoot<A : DomainAbstractAggregateRoot<A>> {
    @Transient
    private val domainEvents: MutableList<Any> = mutableListOf()

    protected fun <T> registerEvent(event: T): T {
        requireNotNull(event) { "Domain event must not be null" }
        domainEvents.add(event)
        return event
    }

    @AfterEventPublication
    protected fun clearDomainEvents() {
        domainEvents.clear()
    }

    @DomainEvents
    protected fun domainEvents(): List<Any> = domainEvents.toList()

    protected fun andEventsFrom(aggregate: A): A {
        domainEvents.addAll(aggregate.domainEvents())
        @Suppress("UNCHECKED_CAST")
        return this as A
    }

    protected fun andEvent(event: Any): A {
        registerEvent(event)
        @Suppress("UNCHECKED_CAST")
        return this as A
    }
}