package com.few.generator.service

import com.few.generator.repository.GenRepository
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDate

@Component
class DateProvider(
    private val genRepository: GenRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun getTargetDate(): LocalDate {
        val today = LocalDate.now(clock)
        val candidate = genRepository.findFirstLimit(1)[0].createdAt?.toLocalDate()
        return candidate?.takeUnless { it.isAfter(today) } ?: today
    }
}