package event.domain

import org.springframework.data.domain.AbstractAggregateRoot

/**
 * Domain abstract aggregate root
 *
 * @see org.springframework.data.domain.AbstractAggregateRoot
 */
abstract class DomainAbstractAggregateRoot<A : DomainAbstractAggregateRoot<A>> : AbstractAggregateRoot<A>()