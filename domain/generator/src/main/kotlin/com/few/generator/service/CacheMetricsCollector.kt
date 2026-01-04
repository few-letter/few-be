package com.few.generator.service

import com.few.generator.config.CacheNames
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.search.Search
import org.springframework.stereotype.Service

@Service
class CacheMetricsCollector(
    private val meterRegistry: MeterRegistry,
) {
    private val log = KotlinLogging.logger {}

    data class CacheStats(
        val cacheName: String,
        val hits: Double,
        val misses: Double,
        val puts: Double,
        val evictions: Double,
        val hitRate: Double,
    ) {
        val totalRequests: Double
            get() = hits + misses

        fun toFormattedString(): String =
            buildString {
                appendLine("*Cache Name:* `$cacheName`")
                appendLine("*Total Requests:* ${totalRequests.toLong()}")
                appendLine("*Hits:* ${hits.toLong()} | *Misses:* ${misses.toLong()}")
                appendLine("*Hit Rate:* ${String.format("%.2f%%", hitRate * 100)}")
                appendLine("*Puts:* ${puts.toLong()} | *Evictions:* ${evictions.toLong()}")
            }
    }

    /**
     * Gen 캐시에 대한 현재 누적 통계를 수집합니다.
     */
    fun collectGenCacheStats(): CacheStats? = collectCacheStats(CacheNames.GEN_CACHE)

    private fun collectCacheStats(cacheName: String): CacheStats? =
        try {
            val hits = getCacheGets(cacheName, "hit")
            val misses = getCacheGets(cacheName, "miss")
            val puts = getCachePuts(cacheName)
            val evictions = getCacheEvictions(cacheName)

            val totalRequests = hits + misses
            val hitRate = if (totalRequests > 0) hits / totalRequests else 0.0

            CacheStats(
                cacheName = cacheName,
                hits = hits,
                misses = misses,
                puts = puts,
                evictions = evictions,
                hitRate = hitRate,
            )
        } catch (e: Exception) {
            log.error(e) { "Failed to collect cache stats for $cacheName" }
            null
        }

    private fun getCacheGets(
        cacheName: String,
        result: String,
    ): Double =
        Search
            .`in`(meterRegistry)
            .name("cache.gets")
            .tag("name", cacheName)
            .tag("result", result)
            .functionCounter()
            ?.count() ?: 0.0

    private fun getCachePuts(cacheName: String): Double =
        Search
            .`in`(meterRegistry)
            .name("cache.puts")
            .tag("name", cacheName)
            .functionCounter()
            ?.count() ?: 0.0

    private fun getCacheEvictions(cacheName: String): Double =
        Search
            .`in`(meterRegistry)
            .name("cache.evictions")
            .tag("name", cacheName)
            .functionCounter()
            ?.count() ?: 0.0
}