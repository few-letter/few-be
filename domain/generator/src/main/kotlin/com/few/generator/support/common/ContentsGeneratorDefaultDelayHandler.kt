package com.few.generator.support.common

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!prd")
class ContentsGeneratorDefaultDelayHandler : ContentsGeneratorDelayHandler {
    override fun delay() { // No-op
    }
}