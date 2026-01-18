package com.few.generator.support.common

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("prd")
class ContentsGeneratorPrdDelayHandler : ContentsGeneratorDelayHandler {
    override fun delay() = Thread.sleep((0..15).random().toLong() * 60 * 1000)
}