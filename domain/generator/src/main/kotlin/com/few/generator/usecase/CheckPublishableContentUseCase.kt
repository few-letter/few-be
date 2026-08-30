package com.few.generator.usecase

import com.few.generator.repository.GenRepository
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.out.CheckPublishableContentUseCaseOut
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CheckPublishableContentUseCase(
    private val genRepository: GenRepository,
) {
    @GeneratorTransactional(readOnly = true)
    fun execute(): CheckPublishableContentUseCaseOut {
        val now = LocalDateTime.now()
        val yesterday = now.minusDays(1)

        val unpublishedGens =
            genRepository.findAllByCreatedAtBetweenAndNotPublishedViaSkills(yesterday, now)

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