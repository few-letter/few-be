package com.few.generator.service

import com.few.generator.event.dto.ContentsSchedulingEventDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import web.handler.exception.BadRequestException
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

@Service
class SchedulingManagementService(
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)

    fun executeWithConcurrencyControl(operation: () -> List<GroupContentResult>): List<GroupContentResult> {
        if (!isRunning.compareAndSet(false, true)) {
            throw BadRequestException("Group scheduling is already running. Please try again later.")
        }

        return try {
            executeScheduling(operation)
        } finally {
            isRunning.set(false)
        }
    }

    private fun executeScheduling(operation: () -> List<GroupContentResult>): List<GroupContentResult> {
        val startTime = LocalDateTime.now()
        var isSuccess = true
        var creationTimeSec = 0.0
        var exception: Throwable? = null
        var groupResults = emptyList<GroupContentResult>()

        runCatching {
            creationTimeSec =
                measureTimeMillis {
                    groupResults = operation()
                }.msToSeconds()
        }.onFailure { ex ->
            isSuccess = false
            log.error(ex) { "그룹 스케줄링 중 오류 발생" }
            exception = ex
        }.also {
            logSchedulingResult(isSuccess, startTime, creationTimeSec, exception, groupResults)
            publishSchedulingEvent(isSuccess, startTime, creationTimeSec, exception, groupResults)
        }

        return groupResults
    }

    private fun logSchedulingResult(
        isSuccess: Boolean,
        startTime: LocalDateTime,
        creationTimeSec: Double,
        exception: Throwable?,
        groupResults: List<GroupContentResult>,
    ) {
        log.info {
            buildString {
                appendLine("✅ isSuccess: $isSuccess")
                appendLine("✅ 시작 시간: $startTime")
                appendLine("✅ 소요 시간: $creationTimeSec")
                appendLine("✅ message: ${exception?.message}")
                appendLine("✅ groupResult: $groupResults")
            }
        }
    }

    private fun publishSchedulingEvent(
        isSuccess: Boolean,
        startTime: LocalDateTime,
        creationTimeSec: Double,
        exception: Throwable?,
        groupResults: List<GroupContentResult>,
    ) {
        applicationEventPublisher.publishEvent(
            ContentsSchedulingEventDto(
                isSuccess = isSuccess,
                startTime = startTime,
                totalTime = "${creationTimeSec}초",
                message = exception?.cause?.message ?: "그룹 스케줄링이 성공적으로 완료되었습니다.",
                result = groupResults.joinToString(separator = "\n") { it.toString() },
            ),
        )
    }

    private fun Long.msToSeconds(): Double = this / 1000.0
}