package com.few.generator.service

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.core.gpt.prompt.schema.Texts
import com.few.generator.domain.vo.ProvisioningContents
import com.few.generator.domain.vo.RawContents
import com.google.gson.Gson
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class ProvisioningService(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    private val log = KotlinLogging.logger {}

    fun createAndSave(rawContents: RawContents): ProvisioningContents {
        val bodyTexts: Texts = makeBodyTexts(rawContents.title, rawContents.rawTexts)
        val coreTexts: Texts = makeCoreTexts(rawContents.title, bodyTexts)

        return ProvisioningContents(
            coreTextsJson = gson.toJson(coreTexts.texts),
            category = rawContents.category,
            region = rawContents.region,
        )
    }

    private fun makeBodyTexts(
        title: String,
        rawTexts: String,
    ): Texts {
        val prompt = promptGenerator.toBodyTexts(title, rawTexts)
        return chatGpt.ask(prompt) as Texts
    }

    private fun makeCoreTexts(
        title: String,
        bodyTexts: Texts,
    ): Texts {
        val prompt = promptGenerator.toCoreTexts(title, bodyTexts)
        return chatGpt.ask(prompt) as Texts
    }
}