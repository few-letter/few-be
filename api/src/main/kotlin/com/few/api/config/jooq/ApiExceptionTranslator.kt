package com.few.api.config.jooq

import org.jooq.ExecuteContext
import org.jooq.ExecuteListener
import org.springframework.jdbc.support.SQLExceptionTranslator

class ApiExceptionTranslator(
    private val translator: SQLExceptionTranslator,
) : ExecuteListener {
    override fun exception(context: ExecuteContext) {
        context.exception(
            translator
                .translate("Access database using Jooq", context.sql(), context.sqlException()!!),
        )
    }
}