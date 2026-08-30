package com.few.generator.service

import com.few.common.domain.Category
import com.few.common.domain.Region
import com.few.generator.domain.Gen
import com.few.generator.repository.GenRepository
import com.few.generator.support.jpa.GeneratorTransactional
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import java.time.LocalDateTime

@Service
class GenService(
    private val genRepository: GenRepository,
) {
    @GeneratorTransactional(propagation = Propagation.REQUIRES_NEW)
    fun saveWithNewTx(gen: Gen): Gen = genRepository.save(gen)

    fun findByUrl(url: String): Gen? = genRepository.findByUrl(url)

    @GeneratorTransactional(readOnly = true, propagation = Propagation.REQUIRED)
    fun findByIdInOrderByIdAsc(ids: List<Long>): List<Gen> = genRepository.findByIdInOrderByIdAsc(ids)

    fun findLatestGen(): Gen = genRepository.findFirstLimit(1, Region.LOCAL.code)[0]

    @GeneratorTransactional(readOnly = true, propagation = Propagation.REQUIRED)
    fun findAllByCreatedAtBetweenAndRegion(
        start: LocalDateTime,
        end: LocalDateTime,
        region: Region = Region.LOCAL,
    ): List<Gen> = genRepository.findAllByCreatedAtBetweenAndRegion(start, end, region.code)

    @GeneratorTransactional(readOnly = true, propagation = Propagation.REQUIRED)
    fun findAllByCreatedAtTodayAndCategoryAndRegion(
        category: Category,
        region: Region = Region.LOCAL,
    ): List<Gen> =
        genRepository.findAllByCreatedAtBetweenAndCategoryAndRegion(
            LocalDateTime
                .now()
                .withHour(0)
                .withMinute(0)
                .withSecond(0),
            LocalDateTime
                .now()
                .plusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0),
            category.code,
            region.code,
        )
}