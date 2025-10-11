package com.few.generator.usecase.input

import com.few.common.domain.Category
import com.few.common.domain.Region

data class BrowseContentsUseCaseIn(
    val prevGenId: Long,
    val category: Category? = null,
    val region: Region,
)