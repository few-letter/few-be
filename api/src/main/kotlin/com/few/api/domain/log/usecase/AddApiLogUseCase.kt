package com.few.api.domain.log.usecase

import com.few.api.config.jooq.ApiTransactional
import com.few.api.domain.log.dto.AddApiLogUseCaseIn
import com.few.api.domain.log.repo.LogIfoDao
import com.few.api.domain.log.repo.command.InsertLogCommand
import org.springframework.stereotype.Component

@Component
class AddApiLogUseCase(
    private val logIfoDao: LogIfoDao,
) {
    @ApiTransactional
    fun execute(useCaseIn: AddApiLogUseCaseIn) {
        logIfoDao.insertLogIfo(InsertLogCommand(useCaseIn.history))
    }
}