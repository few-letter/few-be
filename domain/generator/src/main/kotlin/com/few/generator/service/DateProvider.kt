package com.few.generator.service

import com.few.generator.repository.GenRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class DateProvider(
    private val genRepository: GenRepository,
) {
    fun getTargetDate(): LocalDate = genRepository.findLatestCreatedAt()?.toLocalDate() ?: LocalDate.now()
}