package com.few.generator.usecase

import com.few.generator.domain.Category
import com.few.generator.repository.GenRepository
import com.few.generator.service.GroupGenPersistenceService
import com.few.generator.service.GroupGenerationService
import com.few.generator.service.GroupPromptResult
import com.few.generator.service.SchedulingManagementService
import com.few.generator.support.jpa.GeneratorTransactional
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class GroupSchedulingUseCase(
    private val genRepository: GenRepository,
    private val groupGenerationService: GroupGenerationService,
    private val groupGenPersistenceService: GroupGenPersistenceService,
    private val schedulingManagementService: SchedulingManagementService,
) {
    private val log = KotlinLogging.logger {}

    @Scheduled(cron = "\${scheduling.cron.group}")
    @GeneratorTransactional
    fun execute() {
        schedulingManagementService.executeWithConcurrencyControl {
            generateGroupsForAllCategories()
        }
    }

    private fun generateGroupsForAllCategories(): List<GroupPromptResult> {
        val timeRange = getTodayTimeRange()

        return Category.entries.map { category ->
            log.info { "카테고리 ${category.title} 그룹 생성 시작" }

            val gens =
                genRepository.findAllByCreatedAtBetweenAndCategory(
                    timeRange.first,
                    timeRange.second,
                    category.code,
                )

            val groupPromptResult = groupGenerationService.generateGroupContent(gens)
            groupGenPersistenceService.saveGroupGen(category, groupPromptResult)

            log.info { "카테고리 ${category.title} 그룹 생성 완료" }
            groupPromptResult
        }
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
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
        return start to end
    }
}