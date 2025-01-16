package event.domain

import event.domain.DomainEventPublishingMethod.Companion.NONE
import event.domain.util.AnnotationDetectionMethodCallback
import org.jmolecules.ddd.annotation.AggregateRoot
import org.springframework.context.ApplicationEventPublisher
import org.springframework.lang.Nullable
import org.springframework.modulith.events.core.EventPublicationRepository
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Method
import java.time.Instant
import java.util.function.Supplier

class DomainEventPublishingMethod(
    private val type: Class<*>,
    private val publishingMethod: Method?,
    private val clearingMethod: Method? = null,
) {
    companion object {
        val NONE = DomainEventPublishingMethod(Any::class.java, null, null)

        fun of(type: Class<*>): DomainEventPublishingMethod {
            if (!type.superclass.isAssignableFrom(DomainAbstractAggregateRoot::class.java)) {
                throw IllegalArgumentException("Type must extend DomainAbstractAggregateRoot: $type")
            }

            if (!type.isAnnotationPresent(AggregateRoot::class.java)) {
                throw IllegalArgumentException("Type must be annotated with @AggregateRoot: $type")
            }

            val result =
                from(
                    type,
                    getDetector(type, DomainEvents::class.java),
                    Supplier {
                        getDetector(
                            type,
                            AfterEventPublication::class.java,
                        )
                    },
                )
            return result
        }
    }

    fun publishEventsFrom(
        aggregates: Iterable<*>,
        publisher: ApplicationEventPublisher,
        eventPublicationRepository: EventPublicationRepository,
    ) {
        for (aggregateRoot in aggregates) {
            if (!type.isInstance(aggregateRoot)) {
                continue
            }

            for (event in asCollection(ReflectionUtils.invokeMethod(publishingMethod!!, aggregateRoot), null)) {
                publisher.publishEvent(event)
                if (event is TraceAbleEvent) {
                    eventPublicationRepository.create(
                        EventPublication(
                            event = event,
                            targetIdentifier = event.targetIdentifier,
                            publicationDate = Instant.now(),
                        ),
                    )
                }
            }

            if (clearingMethod != null) {
                ReflectionUtils.invokeMethod(clearingMethod, aggregateRoot)
            }
        }
    }
}

private fun from(
    type: Class<*>,
    publishing: AnnotationDetectionMethodCallback<*>,
    clearing: Supplier<AnnotationDetectionMethodCallback<*>>,
): DomainEventPublishingMethod {
    if (!publishing.hasFoundAnnotation()) {
        return NONE
    }

    val eventMethod = publishing.getMethod()!!
    ReflectionUtils.makeAccessible(eventMethod)

    return DomainEventPublishingMethod(
        type,
        eventMethod,
        getClearingMethod(clearing.get()),
    )
}

@Nullable
private fun getClearingMethod(clearing: AnnotationDetectionMethodCallback<*>): Method? {
    if (!clearing.hasFoundAnnotation()) {
        return null
    }

    val method = clearing.getRequiredMethod()
    ReflectionUtils.makeAccessible(method)

    return method
}

private fun <T : Annotation> getDetector(
    type: Class<*>,
    annotation: Class<T>,
): AnnotationDetectionMethodCallback<T> {
    val callback = AnnotationDetectionMethodCallback(annotation)
    ReflectionUtils.doWithMethods(type, callback)

    return callback
}