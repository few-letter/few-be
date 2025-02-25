package com.few.generator.service

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.ProvisioningPromptGenerator
import com.few.generator.core.gpt.prompt.schema.Texts
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
        val bodyTexts: Texts = makeBodyTexts(rawContents.title, rawContents.description, rawContents.rawTexts)
        val coreTexts: Texts = makeCoreTexts(rawContents.title, rawContents.description, bodyTexts)

        return provisioningContentsRepository.save(
            ProvisioningContents( // TODO: completion의 ID 저장
                bodyTextsJson =
                    bodyTexts.texts.joinToString( // TODO: DB 저장 타입 등 정의, 수정 필요
                        prefix = "[",
                        postfix = "]",
                    ) { "\"$it\"" },
                coreTextsJson =
                    coreTexts.texts.joinToString(
                        prefix = "[",
                        postfix = "]",
                    ) { "\"$it\"" },
            ),
        )
    }

    private fun makeBodyTexts(
        title: String,
        description: String,
        rawTexts: String,
    ): Texts {
        val prompt = provisioningPromptGenerator.createBodyTexts(title, description, rawTexts)
        val completion = chatGpt.ask(prompt)
        return completion.getFirstChoiceMessage(prompt.response_format.classType)
    }

    private fun makeCoreTexts(
        title: String,
        description: String,
        bodyTexts: Texts,
    ): Texts {
        val prompt = provisioningPromptGenerator.createCoreTexts(title, description, bodyTexts)
        val completion = chatGpt.ask(prompt)
        return completion.getFirstChoiceMessage(prompt.response_format.classType)
    }
}