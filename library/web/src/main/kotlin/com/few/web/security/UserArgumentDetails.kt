package com.few.web.security

import com.few.security.TokenUserDetails
import org.springframework.security.core.GrantedAuthority

class UserArgumentDetails(
    val isAuth: Boolean,
    authorities: List<GrantedAuthority>,
    id: String,
    email: String,
) : TokenUserDetails(
        authorities = authorities,
        id = id,
        email = email,
    )