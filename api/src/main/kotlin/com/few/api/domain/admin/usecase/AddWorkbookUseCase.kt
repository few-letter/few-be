package com.few.api.domain.admin.usecase

import com.few.api.config.jooq.ApiTransactional
import com.few.api.domain.admin.usecase.dto.AddWorkbookUseCaseIn
import com.few.api.domain.admin.usecase.dto.AddWorkbookUseCaseOut
import com.few.api.domain.common.exception.InsertException
import com.few.api.domain.workbook.repo.WorkbookDao
import com.few.api.domain.workbook.repo.command.InsertWorkBookCommand
import org.springframework.stereotype.Component

@Component
class AddWorkbookUseCase(
    private val workbookDao: WorkbookDao,
) {
    @ApiTransactional
    fun execute(useCaseIn: AddWorkbookUseCaseIn): AddWorkbookUseCaseOut {
        val workbookId =
            workbookDao.insertWorkBook(
                InsertWorkBookCommand(
                    title = useCaseIn.title,
                    mainImageUrl = useCaseIn.mainImageUrl,
                    category = useCaseIn.category,
                    description = useCaseIn.description,
                ),
            ) ?: throw InsertException("workbook.insertfail.record")

        return AddWorkbookUseCaseOut(workbookId)
    }
}