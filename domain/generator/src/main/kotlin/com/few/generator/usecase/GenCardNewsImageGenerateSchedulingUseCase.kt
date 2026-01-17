package com.few.generator.usecase

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.instagram.NewsContent
import com.few.generator.core.instagram.SingleNewsCardGenerator
import com.few.generator.event.CardNewsImageGeneratedEvent
import com.few.generator.event.GenSchedulingCompletedEvent
import com.few.generator.service.GenService
import com.few.generator.support.jpa.GeneratorTransactional
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

@Component
class GenCardNewsImageGenerateSchedulingUseCase(
    private val genService: GenService,
    private val singleNewsCardGenerator: SingleNewsCardGenerator,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}
    private val isRunning = AtomicBoolean(false)

    @Async("generatorSchedulingExecutor")
    @EventListener
    fun onGenSchedulingCompleted(event: GenSchedulingCompletedEvent) {
        log.info { "${event.region.name} Gen 스케줄링 완료 감지, 카드뉴스 이미지 생성 자동 시작" }

        if (!isRunning.compareAndSet(false, true)) {
            log.warn { "${event.region.name} 이미지 생성이 이미 실행 중입니다." }
            return
        }

        try {
            // 직전에 생성된 GEN이 commit되기를 기다림
            Thread.sleep(3000)
            execute(event.region)
        } catch (e: Exception) {
            log.error(e) { "${event.region.name} Gen 완료 후 자동 카드뉴스 이미지 생성 실패: ${e.message}" }
        } finally {
            isRunning.set(false)
        }
    }

    @GeneratorTransactional(readOnly = true)
    fun doExecute(region: Region): Map<Category, List<String>> {
        // 오늘 생성된 Gen 조회 (00:00:00 ~ 23:59:59)
        val today = LocalDateTime.now()
        val startOfDay =
            today
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
        val endOfDay =
            today
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)

        val gens = genService.findAllByCreatedAtBetweenAndRegion(startOfDay, endOfDay, region)

        if (gens.isEmpty()) {
            log.warn { "오늘 생성된 Gen이 없습니다." }
            return emptyMap()
        }

        log.info { "오늘 생성된 Gen ${gens.size}개를 찾았습니다. 이미지 생성을 시작합니다." }

        val generatedImagesByCategory = mutableMapOf<Category, MutableList<String>>()

        gens.forEachIndexed { index, gen ->
            try {
                // Parse highlight texts from JSON
                val highlightTexts =
                    try {
                        val type = object : TypeToken<List<String>>() {}.type
                        gson.fromJson<List<String>>(gen.highlightTexts, type)
                    } catch (e: Exception) {
                        log.warn(e) { "Gen ${gen.id} 하이라이트 텍스트 파싱 실패, 빈 리스트 사용" }
                        emptyList()
                    }

                // Get category
                val category = Category.from(gen.category)

                // Convert Gen to NewsContent
                val newsContent =
                    NewsContent(
                        headline = gen.headline,
                        summary = gen.summary,
                        category = category.title,
                        createdAt = gen.createdAt ?: LocalDateTime.now(),
                        highlightTexts = highlightTexts,
                    )

                // Generate image file path: {date}_{categoryEnglish}_{genId}.png
                val dateStr = (gen.createdAt ?: LocalDateTime.now()).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                val fileName = "gen_images/${dateStr}_${category.englishName}_${gen.id}.png"

                // Generate image using SingleNewsCardGenerator
                val success = singleNewsCardGenerator.generateImage(newsContent, fileName)

                if (success) {
                    generatedImagesByCategory.getOrPut(category) { mutableListOf() }.add(fileName)
                    log.info { "[${index + 1}/${gens.size}] Gen ${gen.id} 이미지 생성 완료: $fileName (카테고리: ${category.title})" }
                } else {
                    log.error { "[${index + 1}/${gens.size}] Gen ${gen.id} 이미지 생성 실패" }
                }
            } catch (e: Exception) {
                log.error(e) { "[${index + 1}/${gens.size}] Gen ${gen.id} 이미지 생성 중 예외 발생" }
            }
        }

        val totalGenerated = generatedImagesByCategory.values.sumOf { it.size }
        log.info { "이미지 생성 완료: 총 ${gens.size}개 중 ${totalGenerated}개 성공 (${generatedImagesByCategory.size}개 카테고리)" }
        return generatedImagesByCategory
    }

    fun execute(region: Region) {
        val startTime = LocalDateTime.now()
        var isSuccess = true
        var executionTimeSec = 0.0
        var exception: Throwable? = null
        var imagePathsByCategory = emptyMap<Category, List<String>>()

        runCatching {
            executionTimeSec =
                measureTimeMillis {
                    imagePathsByCategory = doExecute(region)
                }.msToSeconds()
        }.onFailure { ex ->
            isSuccess = false
            log.error(ex) { "${region.name} 이미지 생성 중 오류 발생" }
            exception = ex
        }.also {
            val totalImages = imagePathsByCategory.values.sumOf { it.size }
            log.info {
                buildString {
                    appendLine("🖼️ ${region.name} Gen 카드뉴스 이미지 생성 완료")
                    appendLine("✅ 성공 여부: $isSuccess")
                    appendLine("✅ 시작 시간: $startTime")
                    appendLine("✅ 소요 시간: ${executionTimeSec}초")
                    if (isSuccess) {
                        appendLine("✅ 생성된 이미지 개수: $totalImages (${imagePathsByCategory.size}개 카테고리)")
                        if (imagePathsByCategory.isNotEmpty()) {
                            appendLine("✅ 카테고리별 생성된 이미지:")
                            imagePathsByCategory.forEach { (category, paths) ->
                                appendLine("   [${category.title}] ${paths.size}개")
                                paths.forEach { path ->
                                    appendLine("      - $path")
                                }
                            }
                        }
                    }
                    if (!isSuccess) appendLine("❌ 오류: ${exception?.message}")
                }
            }

            // 이미지 생성 성공 시 S3 업로드 이벤트 발행
            if (isSuccess && imagePathsByCategory.isNotEmpty()) {
                applicationEventPublisher.publishEvent(
                    CardNewsImageGeneratedEvent(
                        region = region,
                        imagePathsByCategory = imagePathsByCategory,
                    ),
                )
                val totalCount = imagePathsByCategory.values.sumOf { it.size }
                log.info { "${region.name} 카드뉴스 이미지 생성 완료 이벤트 발행: ${totalCount}개 (${imagePathsByCategory.size}개 카테고리)" }
            }
        }
    }

    private fun Long.msToSeconds(): Double = this / 1000.0
}