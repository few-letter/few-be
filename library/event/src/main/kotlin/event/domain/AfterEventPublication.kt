package event.domain

import org.springframework.aot.hint.annotation.Reflective

@Reflective
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.ANNOTATION_CLASS,
)
annotation class AfterEventPublication