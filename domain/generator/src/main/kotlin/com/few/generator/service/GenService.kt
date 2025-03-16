package com.few.generator.service

import com.few.generator.domain.Gen
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import com.few.generator.repository.GenRepository
import com.few.generator.service.strategy.GenGenerationStrategy
import com.few.generator.service.strategy.Material
import com.few.generator.support.common.Constant
import org.springframework.stereotype.Service

@Service
class GenService(
    private val genRepository: GenRepository,
    private val genGenerationStrategies: Map<String, GenGenerationStrategy>,
) {
    fun create(
        rawContents: RawContents,
        provisioningContents: ProvisioningContents,
    ): List<Gen> {
        val genBasic =
            genGenerationStrategies[Constant.GEN.STRATEGY_NAME_BASIC]!!.generate(
                Material(
                    provisioningContentsId = provisioningContents.id!!,
                    title = rawContents.title,
                    description = rawContents.description,
                    coreTextsJson = provisioningContents.coreTextsJson,
                ),
            )

        val genKorean =
            genGenerationStrategies[Constant.GEN.STRATEGY_NAME_KOREAN]!!.generate(
                Material(
                    provisioningContentsId = provisioningContents.id!!,
                    title = rawContents.title,
                    description = rawContents.description,
                    coreTextsJson = provisioningContents.coreTextsJson,
                    headline = genBasic.headline,
                    summary = genBasic.summary,
                ),
            )

        val genKoreanQuestion =
            genGenerationStrategies[Constant.GEN.STRATEGY_NAME_KOREAN_QUESTION]!!.generate(
                Material(
                    provisioningContentsId = provisioningContents.id!!,
                    title = rawContents.title,
                    description = rawContents.description,
                    coreTextsJson = provisioningContents.coreTextsJson,
                    headline = genBasic.headline,
                    summary = genBasic.summary,
                ),
            )

        val genKoreanLongQuestion =
            genGenerationStrategies[Constant.GEN.STRATEGY_NAME_KOREAN_LONG_QUESTION]!!.generate(
                Material(
                    provisioningContentsId = provisioningContents.id!!,
                    title = rawContents.title,
                    description = rawContents.description,
                    coreTextsJson = provisioningContents.coreTextsJson,
                    headline = genKoreanQuestion.headline,
                    summary = genBasic.summary,
                ),
            )

        /**
         * bulk insert
         */
        return genRepository.saveAll(listOf(genBasic, genKorean, genKoreanQuestion, genKoreanLongQuestion))
    }
}