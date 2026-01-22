package com.few.generator.usecase

import com.few.common.domain.Region
import com.few.generator.core.scrapper.Scrapper
import com.few.generator.service.ContentsCommonGenerationService
import com.few.generator.support.common.ContentsGeneratorDelayHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class LocalGenSchedulingUseCase(
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
    override val region = Region.LOCAL
    override val regionName = "로컬"
    override val schedulingName = "Local News Contents scheduling"
    override val eventTitle = "Local Gen 생성"
}