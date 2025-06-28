package com.few.generator.usecase

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.prompt.schema.Group
import com.few.generator.core.gpt.prompt.schema.Headline
import com.few.generator.core.gpt.prompt.schema.HighlightTexts
import com.few.generator.core.gpt.prompt.schema.Summary
import com.few.generator.domain.Category
import com.few.generator.domain.Gen
import com.few.generator.domain.GroupGen
import com.few.generator.domain.vo.GroupSourceHeadline
import com.few.generator.event.dto.ContentsSchedulingEventDto
import com.few.generator.repository.GenRepository
import com.few.generator.repository.GroupGenRepository
import com.few.generator.repository.ProvisioningContentsRepository
import com.few.generator.repository.RawContentsRepository
import com.few.generator.service.GroupPromptService
import com.few.generator.support.jpa.GeneratorTransactional
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import web.handler.exception.BadRequestException
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.measureTimeMillis

/**
 * 그룹 스케줄링 결과 데이터 클래스
 */
data class GroupPromptResult(
    val group: Group,
    val groupHeadline: Headline,
    val groupSummary: Summary,
    val groupHighlights: HighlightTexts,
    val groupSourceHeadlines: List<GroupSourceHeadline> = emptyList(),
)

@Component
class GroupSchedulingUseCase(
    private val groupPromptService: GroupPromptService,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
    private val genRepository: GenRepository,
    private val groupGenRepository: GroupGenRepository,
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    private val rawContentsRepository: RawContentsRepository,
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

    /**
     * 내부 실행 로직. returnResult=true면 결과 반환, 아니면 스케줄러용(반환 없음)
     */
    private fun doExecute() {
        val startTime = LocalDateTime.now()
        var isSuccess = true
        var creationTimeSec = 0.0
        var exception: Throwable? = null
        val groupResults = mutableListOf<GroupPromptResult>()

        runCatching {
            creationTimeSec =
                measureTimeMillis {
                    val start =
                        LocalDateTime
                            .now()
                            .withHour(0)
                            .withMinute(0)
                            .withSecond(0)
                    val end =
                        LocalDateTime
                            .now()
                            .withHour(23)
                            .withMinute(59)
                            .withSecond(59)
                    Category.entries.forEach {
                        val gens = genRepository.findAllByCreatedAtBetweenAndCategory(start, end, it.code)
                        val result = runGroupPromptsInternal(gens)
                        groupResults.add(result)
                        groupGenRepository.save(
                            GroupGen(
                                category = it.code,
                                groupIndices = result.group.group.joinToString(prefix = "[", postfix = "]"),
                                headline = result.groupHeadline.headline,
                                summary = result.groupSummary.summary,
                                highlightTexts =
                                    result.groupHighlights.highlightTexts.joinToString(
                                        prefix = "[",
                                        postfix = "]",
                                    ),
                                groupSourceHeadlines = result.groupSourceHeadlines,
                            ),
                        )
                    }
                }.msToSeconds()
        }.onFailure { ex ->
            isSuccess = false
            log.error(ex) { "그룹 스케줄링 중 오류 발생" }
            exception = ex
        }.also {
            log.info {
                buildString {
                    appendLine("✅ isSuccess: $isSuccess")
                    appendLine("✅ 시작 시간: $startTime")
                    appendLine("✅ 소요 시간: $creationTimeSec")
                    appendLine("✅ message: ${exception?.cause?.message}")
                    appendLine("✅ groupResult: $groupResults")
                }
            }

            applicationEventPublisher.publishEvent(
                ContentsSchedulingEventDto(
                    isSuccess = isSuccess,
                    startTime = startTime,
                    totalTime = "%.3f".format(creationTimeSec),
                    message = if (isSuccess) "None" else exception?.cause?.message ?: "Unknown error",
                    result = if (isSuccess) "그룹 생성(${groupResults.size})" else "None",
                ),
            )

            if (!isSuccess) {
                throw BadRequestException("그룹 스케줄링에 실패 : ${exception?.cause?.message}")
            }
        }
    }

    /**
     * 실제 그룹 프롬프트 파이프라인 실행
     */
    private fun runGroupPromptsInternal(gens: List<Gen>): GroupPromptResult {
        val webGenDetails =
            gens.map { gen ->
                gen.headline to gen.summary
            }
        val headlines = webGenDetails.map { it.first }
        val provisioningContentsIdWithHeadLines = gens.associate { it.provisioningContentsId to it.headline }
        val rawContentsIdWithHeadline = mutableMapOf<Long, String>()
        provisioningContentsRepository.findAllById(provisioningContentsIdWithHeadLines.map { it.key }).map {
            val provisioningContentsId = it.id
            val headLine = provisioningContentsIdWithHeadLines[provisioningContentsId]!!
            val rawContentsId = it.rawContentsId
            rawContentsIdWithHeadline[rawContentsId] = headLine
        }
        val headLinesWithRawContentsUrl = mutableMapOf<String, String>()
        rawContentsRepository.findAllById(rawContentsIdWithHeadline.keys).map {
            val rawContentsId = it.id
            val headLine = rawContentsIdWithHeadline[rawContentsId]!!
            val url = it.url
            headLinesWithRawContentsUrl[headLine] = url
        }

        val groupSourceHeadlines: List<GroupSourceHeadline> =
            headLinesWithRawContentsUrl.map { (headline, url) ->
                GroupSourceHeadline(
                    headline = headline,
                    url = url,
                )
            }

        val summaries = gens.map { it.summary }

        // 1. 그룹화
        val group = groupPromptService.groupWebGen(webGenDetails)

        // 2. 그룹 헤드라인
        val groupHeadline = groupPromptService.groupHeadline(headlines)

        // 3. 그룹 요약
        val groupSummary =
            groupPromptService.groupSummary(
                groupHeadline = groupHeadline.headline,
                headlines = headlines,
                summaries = summaries,
            )

        // 4. 그룹 하이라이트
        val groupHighlights =
            groupPromptService.groupHighlights(
                groupSummary = groupSummary.summary,
            )

        return GroupPromptResult(
            group = group,
            groupHeadline = groupHeadline,
            groupSummary = groupSummary,
            groupHighlights = groupHighlights,
            groupSourceHeadlines = groupSourceHeadlines,
        )
    }

    private fun Long.msToSeconds(): Double = this / 1000.0
}