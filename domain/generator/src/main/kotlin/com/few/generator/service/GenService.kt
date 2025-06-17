package com.few.generator.service

import com.few.generator.domain.Category
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
        rawContent: RawContents,
        provisioningContent: ProvisioningContents,
        typeCodes: Set<Int>,
    ): List<Gen> {
        log.info { "Trying to Generate ${typeCodes.size} Gen Types..." }

        val generatedResults =
            typeCodes.map { typeCode ->
                val genType = GenType.from(typeCode)

                if (!genGenerationStrategies.containsKey(genType.title)) {
                    throw BadRequestException("지원하지 않는 gen 타입입니다. Gen type code: $genType")
                }

                log.info { "Trying to Generate Gen... : ${genType.title} (${genType.code})" }

                genGenerationStrategies[genType.title]!!.generate(
                    Material(
                        // from rawContents
                        title = rawContent.title,
                        description = rawContent.description,
                        // from provisioningContents
                        coreTextsJson = provisioningContent.coreTextsJson,
                        provisioningContentsId = provisioningContent.id!!,
                        category = Category.from(provisioningContent.category),
                    ),
                )
            }

        /**
         * bulk insert
         */
        return genRepository.saveAll(generatedResults)
    }

    fun create(
        rawContent: RawContents,
        provisioningContent: ProvisioningContents,
    ): Gen {
        log.info { "Craete GEN with default gen type(STRATEGY_NAME_SHORT)..." }

        return genRepository.save(
            genGenerationStrategies[GenType.STRATEGY_NAME_SHORT.title]!!.generate(
                Material(
                    // from rawContents
                    title = rawContent.title,
                    description = rawContent.description,
                    // from provisioningContents
                    coreTextsJson = provisioningContent.coreTextsJson,
                    provisioningContentsId = provisioningContent.id!!,
                    category = Category.from(provisioningContent.category),
                ),
            ),
        )
    }

    fun getByProvisioningContentsId(provisioningContentsId: Long): Set<Gen> =
        HashSet<Gen>(genRepository.findByProvisioningContentsId(provisioningContentsId))
}