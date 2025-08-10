package com.few.web.security.filter.token

import com.few.security.exception.SecurityAccessTokenInvalidException
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.regex.Pattern

object AccessTokenResolver {
    private val log = KotlinLogging.logger {}
    private val PATTERN_AUTHORIZATION_HEADER = Pattern.compile("^[Bb]earer (.*)$")

    fun resolve(authorization: String): String {
        val matcher = PATTERN_AUTHORIZATION_HEADER.matcher(authorization)
        if (!matcher.matches()) {
            val exception: SecurityAccessTokenInvalidException =
                getAccessTokenInvalidException("Authorization header is not a Bearer token")
            throw exception
        }
        return matcher.group(1)
    }

    private fun getAccessTokenInvalidException(message: String): SecurityAccessTokenInvalidException {
        val exception = SecurityAccessTokenInvalidException(message)
        log.warn { exception.message }
        return exception
    }
}