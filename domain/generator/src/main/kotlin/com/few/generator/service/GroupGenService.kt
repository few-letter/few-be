package com.few.generator.service

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.domain.Category
import com.few.generator.domain.GroupGen
import com.few.generator.repository.GenRepository
import com.few.generator.repository.GroupGenRepository
import com.few.generator.repository.ProvisioningContentsRepository
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class GroupGenService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    private val genRepository: GenRepository,
    private val groupGenRepository: GroupGenRepository,
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    private val keyWordsService: KeyWordsService,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun createGroupGen(category: Category): GroupGen {
        log.info { "그룹 생성 시작: category=${category.title}" }

        val timeRange = getTodayTimeRange()
        val gens =
            genRepository.findAllByCreatedAtBetweenAndCategory(
                timeRange.first,
                timeRange.second,
                category.code,
            )

        if (gens.isEmpty()) {
            log.warn { "카테고리 ${category.title}에 대한 Gen이 없습니다." }
            return createEmptyGroupGen(category)
        }

        log.info { "카테고리 ${category.title}에서 ${gens.size}개 Gen 발견, 키워드 추출 시작" }

        // 각 Gen에 대해 키워드 추출
        val genDetails =
            gens.map { gen ->
                val coreTexts =
                    provisioningContentsRepository
                        .findById(gen.provisioningContentsId)
                        .orElse(null)
                        ?.coreTextsJson ?: "키워드 없음"

                val keyWords = keyWordsService.generateKeyWords(coreTexts)
                log.debug { "Gen ${gen.id} 키워드 추출 완료: $keyWords" }

                gen.headline to keyWords
            }

        log.info { "키워드 추출 완료, 그룹화 로직 구현 예정" }
        // TODO: 그룹화 로직 구현
        // TODO: 헤드라인, 요약, 하이라이트 생성 로직 구현

        val groupGen =
            GroupGen(
                category = category.code,
                groupIndices = gson.toJson(emptyList<Int>()),
                headline = "",
                summary = "",
                highlightTexts = gson.toJson(emptyList<String>()),
                groupSourceHeadlines = gson.toJson(emptyList<String>()),
            )

        return groupGenRepository.save(groupGen)
    }

    private fun getTodayTimeRange(): Pair<LocalDateTime, LocalDateTime> {
        val start =
            LocalDateTime
                .now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
        val end =
            LocalDateTime
                .now()
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
        return start to end
    }

    private fun createEmptyGroupGen(category: Category): GroupGen =
        groupGenRepository.save(
            GroupGen(
                category = category.code,
                groupIndices = gson.toJson(emptyList<Int>()),
                headline = "",
                summary = "",
                highlightTexts = gson.toJson(emptyList<String>()),
                groupSourceHeadlines = gson.toJson(emptyList<String>()),
            ),
        )
}