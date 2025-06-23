package com.few.generator.usecase

import com.few.generator.config.GeneratorGsonConfig.Companion.GSON_BEAN_NAME
import com.few.generator.repository.GenRepository
import com.few.generator.repository.ProvisioningContentsRepository
import com.few.generator.repository.RawContentsRepository
import com.few.generator.support.jpa.GeneratorTransactional
import com.few.generator.usecase.out.BrowseContentsUsecaseOuts
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
data class BrowseContentsUseCase(
    private val rawContentsRepository: RawContentsRepository,
    private val provisioningContentsRepository: ProvisioningContentsRepository,
    private val genRepository: GenRepository,
    @Qualifier(GSON_BEAN_NAME)
    private val gson: Gson,
) {
    @GeneratorTransactional(readOnly = true)
    fun execute(prevContentId: Long): BrowseContentsUsecaseOuts {
        TODO()
    }
}