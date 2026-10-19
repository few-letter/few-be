package com.few.generator.config

import com.few.generator.config.properties.CircuitBreakerProperties
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.TimeUnit

/**
 * 공개 API(/api/v1, /api/v2)에 대한 서킷 브레이커.
 * 5xx 응답/예외가 최근 10회 중 50% 이상이면 60초간 503으로 즉시 거절한다.
 */
@Component
class ApiCircuitBreakerFilter(
    private val properties: CircuitBreakerProperties,
) : OncePerRequestFilter() {
    private val circuitBreaker: CircuitBreaker =
        CircuitBreaker.of(
            "api",
            CircuitBreakerConfig
                .custom()
                .slidingWindowSize(properties.slidingWindowSize)
                .minimumNumberOfCalls(properties.minimumNumberOfCalls)
                .failureRateThreshold(properties.failureRateThreshold)
                .waitDurationInOpenState(properties.waitDurationInOpenState)
                .permittedNumberOfCallsInHalfOpenState(3)
                .build(),
        )

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return !properties.enabled || (!path.startsWith("/api/v1/") && !path.startsWith("/api/v2/"))
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (!circuitBreaker.tryAcquirePermission()) {
            response.status = HttpServletResponse.SC_SERVICE_UNAVAILABLE
            response.contentType = "application/json;charset=UTF-8"
            response.writer.write("""{"message":"일시적으로 요청을 처리할 수 없습니다. 잠시 후 다시 시도해주세요."}""")
            return
        }

        val start = System.nanoTime()
        try {
            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            circuitBreaker.onError(System.nanoTime() - start, TimeUnit.NANOSECONDS, e)
            throw e
        }
        val elapsed = System.nanoTime() - start
        if (response.status >= 500) {
            circuitBreaker.onError(elapsed, TimeUnit.NANOSECONDS, IllegalStateException("HTTP ${response.status}"))
        } else {
            circuitBreaker.onSuccess(elapsed, TimeUnit.NANOSECONDS)
        }
    }
}