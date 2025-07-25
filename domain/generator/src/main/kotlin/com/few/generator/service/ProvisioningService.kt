package com.few.generator.service

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Texts
import com.few.generator.domain.ProvisioningContents
import com.few.generator.domain.RawContents
import com.few.generator.repository.ProvisioningContentsRepository
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import web.handler.exception.BadRequestException

@Service
class ProvisioningService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun create(rawContents: RawContents): ProvisioningContents {
        provisioningContentsRepository.findByRawContentsId(rawContents.id!!)?.let {
            throw BadRequestException("이미 생성된 프로비저닝 컨텐츠가 있습니다. ID: ${it.id}")
        }

        val bodyTexts: Texts = makeBodyTexts(rawContents.title, rawContents.description, rawContents.rawTexts)
        val coreTexts: Texts = makeCoreTexts(rawContents.title, rawContents.description, bodyTexts)

        return provisioningContentsRepository.save(
            ProvisioningContents(
                rawContentsId = rawContents.id!!,
                completionIds =
                    mutableListOf(
                        bodyTexts.completionId!!,
                        coreTexts.completionId!!,
                    ),
                bodyTextsJson = gson.toJson(bodyTexts.texts), // TODO: DB 저장 타입 등 정의, 수정 필요
                coreTextsJson = gson.toJson(coreTexts.texts),
                category = rawContents.category,
            ),
        )
    }

    private fun makeBodyTexts(
        title: String,
        description: String,
        rawTexts: String,
    ): Texts {
        val prompt = promptGenerator.toBodyTexts(title, description, rawTexts)
        val texts: Texts = chatGpt.ask(prompt) as Texts
        return texts
    }

    private fun makeCoreTexts(
        title: String,
        description: String,
        bodyTexts: Texts,
    ): Texts {
        val prompt = promptGenerator.toCoreTexts(title, description, bodyTexts)
        val texts = chatGpt.ask(prompt) as Texts
        return texts
    }
}