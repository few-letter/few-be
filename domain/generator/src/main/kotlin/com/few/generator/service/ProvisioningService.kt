package com.few.generator.service

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.ProvisioningPromptGenerator
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import com.few.generator.repository.ProvisioningContentsRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ProvisioningService(
    private val provisioningPromptGenerator: ProvisioningPromptGenerator,
    private val chatGpt: ChatGpt,
    private val provisioningContentsRepository: ProvisioningContentsRepository,
) {
    private val log = KotlinLogging.logger {}

    fun create(rawContents: RawContents): ProvisioningContents {
        val bodyTexts = makeBodyTexts(rawContents.title, rawContents.description, rawContents.rawTexts)
        val coreTexts = makeCoreTexts(rawContents.title, rawContents.description, bodyTexts)

        return provisioningContentsRepository.save(
            ProvisioningContents( // TODO: completion의 ID 저장
                bodyTextsJson = bodyTexts,
                coreTextsJson = coreTexts,
            ),
        )
    }

    private fun makeBodyTexts(
        title: String,
        description: String,
        rawTexts: String,
    ): String {
        val prompt = provisioningPromptGenerator.createBodyTexts(title, description, rawTexts)
        val completion = chatGpt.ask(prompt)
        return completion.getFirstChoiceMessage()
    }

    private fun makeCoreTexts(
        title: String,
        description: String,
        bodyTexts: String,
    ): String {
        val prompt = provisioningPromptGenerator.createCoreTexts(title, description, bodyTexts)
        val completion = chatGpt.ask(prompt)
        return completion.getFirstChoiceMessage()
    }
}