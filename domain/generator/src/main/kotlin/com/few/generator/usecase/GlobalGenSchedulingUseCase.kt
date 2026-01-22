package com.few.generator.usecase

import com.few.common.domain.Region
import com.few.generator.core.scrapper.Scrapper
import com.few.generator.service.ContentsCommonGenerationService
import com.few.generator.support.common.ContentsGeneratorDelayHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class GlobalGenSchedulingUseCase(
    applicationEventPublisher: ApplicationEventPublisher,
    scrapper: Scrapper,
    @Value("\${generator.contents.countByCategory}")
    contentsCountByCategory: Int,
    delayHandler: ContentsGeneratorDelayHandler,
    contentsCommonGenerationService: ContentsCommonGenerationService,
) : AbstractGenSchedulingUseCase(
        applicationEventPublisher,
        scrapper,
        contentsCountByCategory,
        delayHandler,
        contentsCommonGenerationService,
    ) {
    override val region = Region.GLOBAL
    override val regionName = "글로벌"
    override val schedulingName = "Global news contents scheduling"
    override val eventTitle = "Global Gen 생성"
}