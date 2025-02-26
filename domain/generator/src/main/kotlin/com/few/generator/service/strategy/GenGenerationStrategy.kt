package com.few.generator.service.strategy

import com.few.generator.domain.Gen

interface GenGenerationStrategy {
    fun generate(material: Material): Gen

    companion object {
        const val STRATEGY_NAME_BASIC = "BASIC"
        const val STRATEGY_NAME_KOREAN = "KOREAN"
        const val STRATEGY_NAME_KOREAN_QUESTION = "KOREAN_QUESTION"
        const val STRATEGY_NAME_KOREAN_LONG_QUESTION = "KOREAN_LONG_QUESTION"
    }
}