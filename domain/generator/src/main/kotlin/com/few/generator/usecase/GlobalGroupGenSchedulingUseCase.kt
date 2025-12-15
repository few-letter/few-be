package com.few.generator.usecase

import com.few.common.domain.Region
import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.config.GroupingProperties
import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.specifics.groupgen.GenGroupper
import com.few.generator.service.specifics.groupgen.GroupContentGenerator
import com.few.generator.service.specifics.groupgen.GroupGenMetrics
import com.few.generator.service.specifics.groupgen.KeywordExtractor
import com.few.generator.support.jpa.GeneratorTransactional
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
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

    @Scheduled(cron = "\${scheduling.cron.group}")
    @GeneratorTransactional
    fun execute() {
        executeInternal()
    }
}