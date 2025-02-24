package com.few.generator.service

import com.few.generator.core.gpt.prompt.ProvisioningPromptGenerator
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ProvisioningService(
    private val provisioningPromptGenerator: ProvisioningPromptGenerator,
) {
    private val log = KotlinLogging.logger {}

    fun create(rawContents: RawContents): ProvisioningContents {
        val bodyTexts = makeBodyTexts(rawContents.title, rawContents.description, rawContents.rawTexts)
        val coreTexts = makeCoreTexts(rawContents.title, rawContents.description, bodyTexts)

        return ProvisioningContents(
            bodyTextsJson = bodyTexts,
            coreTextsJson = coreTexts,
        )
    }

    private fun makeBodyTexts(
        title: String,
        description: String,
        rawTexts: String,
    ): String {
        val prompt = provisioningPromptGenerator.createBodyTexts(title, description, rawTexts)
        // TODO: request gpt
        return ""
    }

    private fun makeCoreTexts(
        title: String,
        description: String,
        bodyTexts: String,
    ): String {
        val prompt = provisioningPromptGenerator.createCoreTexts(title, description, bodyTexts)
        // TODO: request gpt
        return ""
    }
}