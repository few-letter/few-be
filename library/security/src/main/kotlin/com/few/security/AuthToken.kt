package com.few.security

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
)