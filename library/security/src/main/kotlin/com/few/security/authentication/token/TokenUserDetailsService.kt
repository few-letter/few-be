package com.few.security.authentication.token

import com.few.security.AuthorityUtils
import com.few.security.TokenClaim
import com.few.security.TokenResolver
import com.few.security.TokenUserDetails
import com.few.security.exception.SecurityAccessTokenInvalidException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.Claims
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

class TokenUserDetailsService(
    private val tokenResolver: TokenResolver,
) : UserDetailsService {
    private val log = KotlinLogging.logger {}

    override fun loadUserByUsername(token: String?): UserDetails {
        val claims: Claims =
            tokenResolver
                .resolve(token)
                ?: throw SecurityAccessTokenInvalidException("Invalid access token. accessToken: $token")

        val id =
            claims
                .get(
                    TokenClaim.MEMBER_ID_CLAIM.key,
                    Integer::class.java,
                ).toLong()

        val roles =
            claims.get(
                TokenClaim.MEMBER_ROLE_CLAIM.key,
                String::class.java,
            )

        val email =
            claims.get(
                TokenClaim.MEMBER_EMAIL_CLAIM.key,
                String::class.java,
            )

        val authorities = AuthorityUtils.toAuthorities(roles)

        return TokenUserDetails(authorities, id.toString(), email)
    }
}