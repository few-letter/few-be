package com.few.api.domain.admin.usecase

import com.few.api.config.jooq.ApiTransactional
import com.few.api.domain.admin.service.AdminArticleMainCardService
import com.few.api.domain.admin.service.dto.AppendWorkbookToArticleMainCardInDto
import com.few.api.domain.admin.usecase.dto.MapArticleUseCaseIn
import com.few.api.domain.workbook.repo.WorkbookDao
import com.few.api.domain.workbook.repo.command.MapWorkBookToArticleCommand
import org.springframework.stereotype.Component

@Component
class MapArticleUseCase(
    private val workbookDao: WorkbookDao,
    private val adminArticleMainCardService: AdminArticleMainCardService,
) {
    @ApiTransactional
    fun execute(useCaseIn: MapArticleUseCaseIn) {
        workbookDao.mapWorkBookToArticle(
            MapWorkBookToArticleCommand(
                useCaseIn.workbookId,
                useCaseIn.articleId,
                useCaseIn.dayCol,
            ),
        )

        adminArticleMainCardService.appendWorkbook(
            AppendWorkbookToArticleMainCardInDto(useCaseIn.articleId, useCaseIn.workbookId),
        )
    }
}