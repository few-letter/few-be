package com.few.generator.controller

import org.springframework.cache.CacheManager
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cache")
class CacheMonitoringController(
    private val cacheManager: CacheManager,
) {
    @GetMapping("/stats")
    fun getCacheStats(): Map<String, Any> {
        val genCache = cacheManager.getCache("genCache")
        val nativeCache = genCache?.nativeCache

        return mapOf(
            "cacheName" to "genCache",
            "cacheType" to (nativeCache?.javaClass?.simpleName ?: "Unknown"),
            "message" to "Cache statistics available in logs. Set logging.level.org.springframework.cache=DEBUG to see cache hits/misses",
        )
    }

    @GetMapping("/clear")
    fun clearCache(): Map<String, String> {
        cacheManager.getCache("genCache")?.clear()
        return mapOf("message" to "Cache cleared successfully")
    }
}