package com.few.generator.usecase

import com.few.common.domain.ContentsType
import com.few.generator.repository.GenRepository
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.out.CheckPublishableContentUseCaseOut
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CheckPublishableContentUseCase(
    private val genRepository: GenRepository,
) {
    /**
     * @param contentsType 조회 대상 ContentsType. null 이면 전체 ContentsType 을 대상으로 한다.
     */
    @GeneratorTransactional(readOnly = true)
    fun execute(contentsType: ContentsType? = null): CheckPublishableContentUseCaseOut {
        val now = LocalDateTime.now()
        val yesterday = now.minusDays(1)

        val targetContentsTypes = contentsType?.let { listOf(it) } ?: ContentsType.entries
        val contentsTypeCodes = targetContentsTypes.map { it.code }

        val unpublishedGens =
            genRepository.findAllByCreatedAtBetweenAndNotPublishedViaSkills(yesterday, now, contentsTypeCodes)

        val contentsTypes =
            unpublishedGens
                .mapNotNull { it.contentsType }
                .distinct()
                .ifEmpty { null }

        return CheckPublishableContentUseCaseOut(
            hasPublishableContent = unpublishedGens.isNotEmpty(),
            count = unpublishedGens.size,
            contentsTypes = contentsTypes,
        )
    }
}