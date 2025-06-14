package com.few.api.domain.article.usecase.transaction

import com.few.api.config.jooq.ApiTransactional
import com.few.api.domain.article.repo.ArticleViewCountDao
import com.few.api.domain.article.repo.command.ArticleViewCountCommand
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation

@Component
class ArticleViewCountTxCase(
    private val articleViewCountDao: ArticleViewCountDao,
) {
    @ApiTransactional(isolation = Isolation.READ_UNCOMMITTED, propagation = Propagation.REQUIRES_NEW)
    fun browseArticleViewCount(articleId: Long): Long =
        (articleViewCountDao.selectArticleViewCount(ArticleViewCountCommand(articleId)) ?: 0L) + 1L
}