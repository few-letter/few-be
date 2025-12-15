package com.few.generator.usecase.input

import com.few.common.domain.Region
import java.time.LocalDate

data class BrowseGroupGenUseCaseIn(
    val date: LocalDate?,
    val region: Region,
)