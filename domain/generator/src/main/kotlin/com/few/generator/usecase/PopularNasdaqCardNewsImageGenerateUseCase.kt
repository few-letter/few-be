package com.few.generator.usecase

import com.few.generator.core.instagram.MainPageCardGenerator
import com.few.generator.core.instagram.NewsContent
import com.few.generator.core.instagram.SingleNewsCardGenerator
import com.few.generator.event.PopularNasdaqCardNewsImageGeneratedEvent
import com.few.generator.event.PopularNasdaqGenSavedEvent
import com.few.generator.service.GenService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class PopularNasdaqCardNewsImageGenerateUseCase(
    private val genService: GenService,
    private val singleNewsCardGenerator: SingleNewsCardGenerator,
    private val mainPageCardGenerator: MainPageCardGenerator,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val log = KotlinLogging.logger {}

    companion object {
        private const val COLOR_KEY = "nasdaq"
    }

    @Async("generatorSchedulingExecutor")
    @EventListener
    fun onPopularNasdaqGenSaved(event: PopularNasdaqGenSavedEvent) {
        log.info { "Popular Nasdaq Gen 저장 완료 감지, 카드뉴스 이미지 생성 시작" }
        try {
            Thread.sleep(3000) // Gen commit 대기
            execute(event.genIdsByStock)
        } catch (e: Exception) {
            log.error(e) { "Popular Nasdaq 카드뉴스 이미지 생성 실패: ${e.message}" }
        }
    }

    fun execute(genIdsByStock: Map<String, List<Long>>) {
        val dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val imagePathsByStock = mutableMapOf<String, List<String>>()
        val mainPageImagePathsByStock = mutableMapOf<String, String>()

        genIdsByStock.forEach { (stockName, genIds) ->
            val gens = genService.findAllByIds(genIds)
            if (gens.isEmpty()) {
                log.warn { "[$stockName] Gen 조회 결과 없음, 건너뜀" }
                return@forEach
            }

            val newsContents =
                gens.map { gen ->
                    NewsContent(
                        headline = gen.headline,
                        summary = gen.summary,
                        category = COLOR_KEY,
                        createdAt = gen.createdAt ?: LocalDateTime.now(),
                        highlightTexts = emptyList(),
                    )
                }

            val safeStockName = stockName.replace(" ", "_")

            val detailPaths = mutableListOf<String>()
            newsContents.forEachIndexed { index, content ->
                val filePath = "gen_images/nasdaq_${dateStr}_${safeStockName}_${index + 1}.png"
                val success = singleNewsCardGenerator.generateImage(content, filePath)
                if (success) {
                    detailPaths.add(filePath)
                    log.info { "[$stockName] 상세 카드 이미지 생성 완료 [${index + 1}/${newsContents.size}]: $filePath" }
                } else {
                    log.error { "[$stockName] 상세 카드 이미지 생성 실패 [${index + 1}/${newsContents.size}]" }
                }
            }

            if (detailPaths.isEmpty()) {
                log.warn { "[$stockName] 상세 카드 이미지 생성 결과 없음, 건너뜀" }
                return@forEach
            }

            val mainPath = "gen_images/nasdaq_${dateStr}_${safeStockName}_main.png"
            val mainSuccess =
                mainPageCardGenerator.generateMainPageImage(
                    colorKey = COLOR_KEY,
                    titleText = "$stockName 주요소식",
                    newsContents = newsContents,
                    outputPath = mainPath,
                )
            if (!mainSuccess) {
                log.error { "[$stockName] 메인 카드 이미지 생성 실패, 해당 종목 스킵" }
                return@forEach
            }

            log.info { "[$stockName] 메인 카드 이미지 생성 완료: $mainPath" }
            imagePathsByStock[stockName] = detailPaths
            mainPageImagePathsByStock[stockName] = mainPath
        }

        if (imagePathsByStock.isNotEmpty()) {
            applicationEventPublisher.publishEvent(
                PopularNasdaqCardNewsImageGeneratedEvent(
                    imagePathsByStock = imagePathsByStock,
                    mainPageImagePathsByStock = mainPageImagePathsByStock,
                ),
            )
            log.info { "Popular Nasdaq 카드뉴스 이미지 생성 완료 이벤트 발행: ${imagePathsByStock.size}개 종목" }
        }
    }
}