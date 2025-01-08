package event.domain

import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationEventPublisher
import org.springframework.lang.Nullable
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
class DomainEventPublishingProxyPostProcessor(
    private val publisher: ApplicationEventPublisher,
) : BeanPostProcessor {
    override fun postProcessAfterInitialization(
        bean: Any,
        beanName: String,
    ): Any? {
        val methods = bean.javaClass.methods
        val domainEventPublishingMethods = methods.filter { it.getAnnotation(PublishEvents::class.java) != null }

        if (domainEventPublishingMethods.isNotEmpty()) {
            val proxyFactory = ProxyFactory(bean)
            domainEventPublishingMethods.forEach { method ->
                val returnType = method.returnType
                val domainEventPublishingMethod = DomainEventPublishingMethod.of(returnType)
                proxyFactory.addAdvice(
                    DomainEventPublishingMethodInterceptor(domainEventPublishingMethod, publisher),
                )
            }
            return proxyFactory.proxy
        }

        return bean
    }
}

fun asCollection(
    @Nullable source: Any?,
    @Nullable method: Method?,
): Iterable<Any> {
    if (source == null) {
        return emptyList()
    }

    if (method != null && method.name.startsWith("saveAll")) {
        return source as Iterable<Any>
    }

    if (MutableCollection::class.java.isInstance(source)) {
        return source as Collection<Any>
    }

    return listOf(source)
}