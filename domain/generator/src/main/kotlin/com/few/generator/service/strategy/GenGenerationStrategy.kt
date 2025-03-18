package com.few.generator.service.strategy

import com.few.generator.domain.Gen

interface GenGenerationStrategy {
    fun generate(material: Material): Gen
}