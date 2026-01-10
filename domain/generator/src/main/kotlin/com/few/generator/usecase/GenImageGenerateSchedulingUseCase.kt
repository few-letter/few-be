package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.service.GenService
import com.few.generator.service.instagram.NewsContent
import com.few.generator.service.instagram.SingleNewsCardGenerator
import com.few.generator.support.jpa.GeneratorTransactional
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

@Component
class GenImageGenerateSchedulingUseCase(
    private val genService: GenService,
    private val singleNewsCardGenerator: SingleNewsCardGenerator,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)

    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    @GeneratorTransactional
    fun scheduledExecute() {
        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "이미지 생성 스케줄링이 이미 실행 중입니다." }
            return
        }

        try {
            executeWithLogging()
        } finally {
            isRunning.set(false)
        }
    }

    fun execute(): String {
        val gen = genService.findLatestGen()

        // Parse highlight texts from JSON
        val highlightTexts =
            try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson<List<String>>(gen.highlightTexts, type)
            } catch (e: Exception) {
                log.warn(e) { "하이라이트 텍스트 파싱 실패, 빈 리스트 사용" }
                emptyList()
            }

        // Convert Gen to NewsContent
        val newsContent =
            NewsContent(
                headline = gen.headline,
                summary = gen.summary,
                category = Category.from(gen.category).title,
                createdAt = gen.createdAt ?: LocalDateTime.now(),
                highlightTexts = highlightTexts,
            )

        // Generate image file path
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val fileName = "gen_images/gen_image_${gen.id}_$timestamp.png"

        // Generate image using SingleNewsCardGenerator
        val success = singleNewsCardGenerator.generateImage(newsContent, fileName)

        if (!success) {
            throw RuntimeException("이미지 생성 실패")
        }

        log.info { "이미지 생성 완료: $fileName" }
        return fileName
    }

    private fun executeWithLogging() {
        val startTime = LocalDateTime.now()
        var isSuccess = true
        var executionTimeSec = 0.0
        var exception: Throwable? = null
        var imagePath = ""

        runCatching {
            executionTimeSec =
                measureTimeMillis {
                    imagePath = execute()
                }.msToSeconds()
        }.onFailure { ex ->
            isSuccess = false
            log.error(ex) { "이미지 생성 중 오류 발생" }
            exception = ex
        }.also {
            log.info {
                buildString {
                    appendLine("🖼️ Gen 이미지 생성 완료")
                    appendLine("✅ 성공 여부: $isSuccess")
                    appendLine("✅ 시작 시간: $startTime")
                    appendLine("✅ 소요 시간: ${executionTimeSec}초")
                    if (isSuccess) appendLine("✅ 이미지 경로: $imagePath")
                    if (!isSuccess) appendLine("❌ 오류: ${exception?.message}")
                }
            }
        }
    }

    private fun Long.msToSeconds(): Double = this / 1000.0
}