package com.few.generator.usecase

import com.few.common.exception.BadRequestException
import com.few.generator.event.dto.ContentsSchedulingEventDto
import com.few.generator.service.GroupGenService
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

@Component
class GroupSchedulingUseCase(
    private val groupGenService: GroupGenService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)

    @Scheduled(cron = "\${scheduling.cron.group}")
    @GeneratorTransactional
    fun execute() {
        if (!isRunning.compareAndSet(false, true)) {
            throw BadRequestException("Group scheduling is already running. Please try again later.")
        }

        try {
            doExecute()
        } finally {
            isRunning.set(false)
        }
    }

    private fun doExecute() {
        val startTime = LocalDateTime.now()
        var isSuccess = true
        var creationTimeSec = 0.0
        var exception: Throwable? = null
        var successCnt = 0

        runCatching {
            creationTimeSec =
                measureTimeMillis {
                    successCnt = createGroupGensForAllCategories()
                }.msToSeconds()
        }.onFailure { ex ->
            isSuccess = false
            log.error(ex) { "그룹 스케줄링 중 오류 발생" }
            exception = ex
        }.also {
            log.info {
                buildString {
                    appendLine("✅ Group Scheduling Result")
                    appendLine("✅ isSuccess: $isSuccess")
                    appendLine("✅ 시작 시간: $startTime")
                    appendLine("✅ 소요 시간: $creationTimeSec")
                    appendLine("✅ message: ${exception?.cause?.message}")
                    append("✅ result: 생성된 그룹 수: $successCnt")
                }
            }

            applicationEventPublisher.publishEvent(
                ContentsSchedulingEventDto(
                    isSuccess = isSuccess,
                    startTime = startTime,
                    totalTime = "%.3f".format(creationTimeSec),
                    message = if (isSuccess) "None" else exception?.cause?.message ?: "Unknown error",
                    result = if (isSuccess) "생성($successCnt)" else "None",
                ),
            )

            if (!isSuccess) {
                throw BadRequestException("그룹 스케줄링에 실패 : ${exception?.cause?.message}")
            }
        }
    }

    private fun createGroupGensForAllCategories(): Int {
        val results = groupGenService.createAllGroupGen()
        return results.size
    }

    private fun Long.msToSeconds(): Double = this / 1000.0
}