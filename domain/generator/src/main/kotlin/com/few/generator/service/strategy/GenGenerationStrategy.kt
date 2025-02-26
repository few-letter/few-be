package com.few.generator.service.strategy

import com.few.generator.domain.Gen
import org.springframework.stereotype.Component

@Component
interface GenGenerationStrategy {
    fun generate(material: Material): Gen
}