package com.few.generator.service.strategy

import com.few.generator.core.gpt.ChatGpt
import com.few.generator.core.gpt.prompt.PromptGenerator
import com.few.generator.domain.Gen
import org.springframework.stereotype.Component

@Component
class KoreanLongQuestionGenGenerationStrategy(
    private val promptGenerator: PromptGenerator,
    private val chatGpt: ChatGpt,
) : GenGenerationStrategy {
    override fun generate(material: Material): Gen {
        TODO("구현")
    }
}