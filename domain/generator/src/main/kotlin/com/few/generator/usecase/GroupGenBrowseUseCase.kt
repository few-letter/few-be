package com.few.generator.usecase

import com.few.generator.controller.response.BrowseGroupGenResponses
import com.few.generator.service.GroupGenBrowseService
import com.few.generator.service.GroupGenResponseMappingService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class GroupGenBrowseUseCase(
    private val groupGenBrowseService: GroupGenBrowseService,
    private val groupGenResponseMappingService: GroupGenResponseMappingService,
) {
    private val log = KotlinLogging.logger {}

    fun execute(date: LocalDate?): BrowseGroupGenResponses {
        log.info { "GroupGen 목록 조회 시작: date=$date" }

        val groupGens = groupGenBrowseService.findGroupGensByDate(date)
        val response = groupGenResponseMappingService.mapToResponses(groupGens)

        log.info { "GroupGen 목록 조회 완료: ${response.groups.size}개" }
        return response
    }
}