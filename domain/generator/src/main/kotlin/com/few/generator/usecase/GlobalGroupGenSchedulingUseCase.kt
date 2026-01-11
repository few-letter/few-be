package com.few.generator.usecase

import com.few.common.domain.Region
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.config.GroupingProperties
import com.few.generator.event.GenSchedulingCompletedEvent
import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.specifics.groupgen.GenGroupper
import com.few.generator.service.specifics.groupgen.GroupContentGenerator
import com.few.generator.service.specifics.groupgen.GroupGenMetrics
import com.few.generator.service.specifics.groupgen.KeywordExtractor
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class GlobalGroupGenSchedulingUseCase(
    applicationEventPublisher: ApplicationEventPublisher,
    genService: GenService,
    provisioningService: ProvisioningService,
    groupingProperties: GroupingProperties,
    @Qualifier(GSON_BEAN_NAME)
    gson: Gson,
    groupGenMetrics: GroupGenMetrics,
    keywordExtractor: KeywordExtractor,
    genGrouper: GenGroupper,
    groupContentGenerator: GroupContentGenerator,
) : AbstractGroupGenSchedulingUseCase(
        applicationEventPublisher,
        genService,
        provisioningService,
        groupingProperties,
        gson,
        groupGenMetrics,
        keywordExtractor,
        genGrouper,
        groupContentGenerator,
    ) {
    override val region = Region.GLOBAL
    override val regionName = "GLOBAL"
    override val eventTitle = "Global Group Gen 생성"

    @Async("generatorSchedulingExecutor")
    @EventListener
    fun onGenSchedulingCompleted(event: GenSchedulingCompletedEvent) {
        if (event.region != Region.GLOBAL) {
            return
        }

        log.info { "Global Gen 스케줄링 완료 감지, Global Group Gen 스케줄링 자동 시작" }

        try {
            super.execute()
        } catch (e: Exception) {
            log.error(e) { "Global Gen 완료 후 자동 Group Gen 실행 실패: ${e.message}" }
        }
    }
}