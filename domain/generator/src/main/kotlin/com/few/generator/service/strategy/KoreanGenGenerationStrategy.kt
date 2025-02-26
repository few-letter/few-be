package com.few.generator.service.strategy

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.domain.Gen
import com.few.generator.service.strategy.GenGenerationStrategy.Companion.STRATEGY_NAME_KOREAN
import org.springframework.stereotype.Component

@Component(STRATEGY_NAME_KOREAN)
class KoreanGenGenerationStrategy(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
) : GenGenerationStrategy {
    override fun generate(material: Material): Gen {
        TODO("구현")
    }
}