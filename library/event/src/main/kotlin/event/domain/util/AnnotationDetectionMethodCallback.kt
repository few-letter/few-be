package event.domain.util

import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.util.Assert
import org.springframework.util.ReflectionUtils
import java.lang.reflect.Method
import kotlin.reflect.KClass

/**
 * Annotation detection method callback
 *
 * @see org.springframework.data.util.AnnotationDetectionMethodCallback
 */
class AnnotationDetectionMethodCallback<A : Annotation>(
    private val annotationType: KClass<A>,
    private val enforceUniqueness: Boolean = false,
) : ReflectionUtils.MethodCallback {
    private var foundMethod: Method? = null
    private var annotation: A? = null

    companion object {
        private const val MULTIPLE_FOUND = "Found annotation %s both on %s and %s; Make sure only one of them is annotated with it"
    }

    init {
        Assert.notNull(annotationType) { "Annotation type must not be null" }
    }

    constructor(annotationType: Class<A>, enforceUniqueness: Boolean = false) : this(
        annotationType.kotlin,
        enforceUniqueness,
    )

    fun getMethod(): Method? = foundMethod

    fun getRequiredMethod(): Method =
        foundMethod
            ?: throw IllegalStateException("No method with annotation ${annotationType.simpleName} found")

    fun getAnnotation(): A? = annotation

    fun hasFoundAnnotation(): Boolean = annotation != null

    override fun doWith(method: Method) {
        if (foundMethod != null && !enforceUniqueness) {
            return
        }

        val foundAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, annotationType.java)

        if (foundAnnotation != null) {
            if (foundMethod != null && enforceUniqueness) {
                throw IllegalStateException(
                    MULTIPLE_FOUND.format(foundAnnotation::class.java.name, foundMethod, method),
                )
            }

            this.annotation = foundAnnotation
            this.foundMethod = method
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> invoke(
        target: Any?,
        vararg args: Any?,
    ): T? {
        val method = this.foundMethod ?: return null
        ReflectionUtils.makeAccessible(method)
        return ReflectionUtils.invokeMethod(method, target, *args) as T?
    }
}