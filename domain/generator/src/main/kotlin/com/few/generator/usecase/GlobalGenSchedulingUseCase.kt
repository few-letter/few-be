package com.few.generator.usecase

import com.few.common.domain.Region
import com.few.generator.core.scrapper.Scrapper
import com.few.generator.service.GenService
import com.few.generator.service.ProvisioningService
import com.few.generator.service.RawContentsService
import com.few.generator.support.common.ContentsGeneratorDelayHandler
import com.few.generator.support.jpa.GeneratorTransactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class GlobalGenSchedulingUseCase(
    rawContentsService: RawContentsService,
    provisioningService: ProvisioningService,
    genService: GenService,
    applicationEventPublisher: ApplicationEventPublisher,
    scrapper: Scrapper,
    @Value("\${generator.contents.countByCategory}")
    contentsCountByCategory: Int,
    delayHandler: ContentsGeneratorDelayHandler,
) : AbstractGenSchedulingUseCase(
        rawContentsService,
        provisioningService,
        genService,
        applicationEventPublisher,
        scrapper,
        contentsCountByCategory,
        delayHandler,
    ) {
    override val region = Region.GLOBAL
    override val regionName = "글로벌"
    override val schedulingName = "Global news contents scheduling"
    override val eventTitle = "Global Gen 생성"

    @Scheduled(cron = "\${scheduling.cron.global-gen}")
    @GeneratorTransactional
    public override fun execute() {
        super.execute()
    }
}