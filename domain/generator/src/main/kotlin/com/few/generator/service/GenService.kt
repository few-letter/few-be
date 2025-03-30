package com.few.generator.service

import com.few.generator.domain.Gen
import com.few.generator.domain.GenType
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import com.few.generator.repository.GenRepository
import com.few.generator.service.strategy.GenGenerationStrategy
import com.few.generator.service.strategy.Material
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import web.handler.exception.BadRequestException

@Service
class GenService(
    private val genRepository: GenRepository,
    private val genGenerationStrategies: Map<String, GenGenerationStrategy>,
) {
    private val log = KotlinLogging.logger {}

    fun create(
        rawContents: RawContents,
        provisioningContents: ProvisioningContents,
        typeCodes: Set<Int>,
    ): List<Gen> {
        log.info { "Trying to Generate ${typeCodes.size} Gen Types..." }

        val generatedResults =
            typeCodes.map { typeCode ->
                val genType = GenType.from(typeCode)

                if (!genGenerationStrategies.containsKey(genType.title)) {
                    throw BadRequestException("지원하지 않는 gen 타입입니다.")
                }

                log.info { "Trying to Generate Gen... : ${genType.title}" }

                genGenerationStrategies[genType.title]!!.generate(
                    Material(
                        // from rawContents
                        title = rawContents.title,
                        description = rawContents.description,
                        // from provisioningContents
                        coreTextsJson = provisioningContents.coreTextsJson,
                        provisioningContentsId = provisioningContents.id!!,
                    ),
                )
            }

        /**
         * bulk insert
         */
        return genRepository.saveAll(generatedResults)
    }

    fun getByProvisioningContentsId(provisioningContentsId: Long): Set<Gen> =
        HashSet<Gen>(genRepository.findByProvisioningContentsId(provisioningContentsId))
}