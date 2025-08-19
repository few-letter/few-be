package com.few.generator.service

import com.few.common.domain.Category
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class GroupGenMetricsService {
    private val log = KotlinLogging.logger {}

    data class GroupGenMetrics(
        val category: Category,
        val totalGens: Int,
        val selectedGens: Int,
        val groupingSuccessful: Boolean,
        val keywordExtractionTimeMs: Long,
        val totalProcessingTimeMs: Long,
        val errorMessage: String? = null,
    )

    fun recordGroupGenMetrics(metrics: GroupGenMetrics) {
        log.info {
            buildString {
                appendLine("📊 GroupGen Metrics")
                appendLine("  Category: ${metrics.category.title}")
                appendLine("  Total Gens: ${metrics.totalGens}")
                appendLine("  Selected Gens: ${metrics.selectedGens}")
                appendLine("  Success Rate: ${if (metrics.groupingSuccessful) "100%" else "0%"}")
                appendLine("  Keyword Extraction Time: ${metrics.keywordExtractionTimeMs}ms")
                appendLine("  Total Processing Time: ${metrics.totalProcessingTimeMs}ms")
                if (metrics.errorMessage != null) {
                    append("  Error: ${metrics.errorMessage}")
                } else {
                    append("  Status: SUCCESS")
                }
            }
        }

        // 여기에 추후 메트릭 시스템(Micrometer, Prometheus 등) 연동 가능
        // meterRegistry.counter("groupgen.total", "category", metrics.category.title).increment()
        // meterRegistry.timer("groupgen.processing.time", "category", metrics.category.title)
        //     .record(metrics.totalProcessingTimeMs, TimeUnit.MILLISECONDS)
    }

    fun recordGroupGenError(
        category: Category,
        errorMessage: String,
        processingTimeMs: Long,
    ) {
        log.error { "❌ GroupGen Error - Category: ${category.title}, Error: $errorMessage, Time: ${processingTimeMs}ms" }

        val errorMetrics =
            GroupGenMetrics(
                category = category,
                totalGens = 0,
                selectedGens = 0,
                groupingSuccessful = false,
                keywordExtractionTimeMs = 0,
                totalProcessingTimeMs = processingTimeMs,
                errorMessage = errorMessage,
            )

        recordGroupGenMetrics(errorMetrics)
    }
}