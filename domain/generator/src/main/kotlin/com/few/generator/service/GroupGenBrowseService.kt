package com.few.generator.service

import com.few.generator.domain.GroupGen
import com.few.generator.repository.GroupGenRepository
import com.few.generator.util.DateTimeUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class GroupGenBrowseService(
    private val groupGenRepository: GroupGenRepository,
) {
    private val log = KotlinLogging.logger {}

    fun findGroupGensByDate(date: LocalDate?): List<GroupGen> {
        val targetDate = date ?: LocalDate.now()
        log.info { "GroupGen 조회 시작: date=$targetDate" }

        val dateRange = DateTimeUtils.createDayRange(targetDate)
        val groupGens =
            groupGenRepository
                .findAllByCreatedAtBetween(dateRange.first, dateRange.second)
                .sortedByDescending { it.createdAt }

        log.info { "GroupGen 조회 완료: ${groupGens.size}개 발견" }
        return groupGens
    }
}